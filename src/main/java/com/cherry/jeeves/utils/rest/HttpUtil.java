package com.cherry.jeeves.utils.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * HTTP 请求工具类 (当前httpclient版本是4.5.2)
 */
public class HttpUtil {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	public static CookieStore cookieStore = new BasicCookieStore();
	private static PoolingHttpClientConnectionManager connMgr;
	private static RequestConfig requestConfig;
//	private static HttpClientBuilder httpBulder;
	public static CloseableHttpClient httpClient;
	
	private static IdleConnectionMonitorThread idleThread = null;
	
	public static ObjectMapper JSON_MAPPER = new ObjectMapper();
	static {
		JSON_MAPPER.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
		JSON_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		JSON_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		JSON_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true); 
	}
	static {
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
		
		// 设置连接池
		connMgr = new PoolingHttpClientConnectionManager(registry);
		// 设置连接池大小
		connMgr.setMaxTotal(100);
		connMgr.setDefaultMaxPerRoute(20);
//		connMgr.setMaxTotal(2);
//		connMgr.setDefaultMaxPerRoute(2);
		
		requestConfig = RequestConfig.custom()
			//连接超时
            .setConnectTimeout(5000)
            //读取超时
            .setSocketTimeout(30000)
            //获取连接池超时
            .setConnectionRequestTimeout(3000)
            .setRedirectsEnabled(false)
            .build();
		
		HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
	        @Override
	        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
	            if (executionCount >= 3) {// 如果已经重试了3次，就放弃
	                return false;
	            }
	            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
	                return true;
	            }
	            if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
	                return false;
	            }
	            if (exception instanceof InterruptedIOException) {// 超时
	                return true;
	            }
	            if (exception instanceof UnknownHostException) {// 目标服务器不可达
	                return false;
	            }
	            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
	                return false;
	            }
	            if (exception instanceof SSLException) {// ssl握手异常
	                return false;
	            }
	            HttpClientContext clientContext = HttpClientContext.adapt(context);
	            HttpRequest request = clientContext.getRequest();
	            // 如果请求是幂等的，就再次尝试
	            if (!(request instanceof HttpEntityEnclosingRequest)) {
	                return true;
	            }
	            return false;
	        }
	    };
	    
	    httpClient = HttpClients.custom()
            .setConnectionManager(connMgr)
            .setRetryHandler(retryHandler)
            .evictExpiredConnections()
            .evictIdleConnections(30, TimeUnit.SECONDS)
            .setDefaultRequestConfig(requestConfig)
            .setDefaultCookieStore(cookieStore)
            .build();
	    
	    idleThread = new IdleConnectionMonitorThread(connMgr);
        idleThread.start();
	}
	
	public static String doGet(String url) {
		return doGet(url, Collections.emptyMap(), Collections.emptyMap());
	}

//	public static String doGet(String url, Map<String, Object> params) {
//		return doGet(url, Collections.emptyMap(), params);
//	}
	
	public static String doGet(String url, Map<String, String> headers) {
		return doGet(url, headers, Collections.emptyMap());
	}
	
	public static String doGet(String url, Map<String, String> headers, Map<String, Object> params) {

		// *) 构建GET请求头
		String apiUrl = getUrlWithParams(url, params);
		HttpGet httpGet = new HttpGet(apiUrl);

		// *) 设置header信息
		if (headers != null && headers.size() > 0) {
			headers.forEach((k, v) -> {
				httpGet.setHeader(k, v);
			});
		}
//		httpGet.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build()); // 禁止重定向
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			if (response == null || response.getStatusLine() == null) {
				return null;
			}

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity, "UTF-8");
			} 
			return null;
		} catch (IOException e) {
			logger.error("doGet", e);
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	
	
	public static String doPost(String apiUrl) {
		return doPost(apiUrl, null);
	}
	
	public static String doPost(String url, Object obj) {
		try {
			return doPost(url, JSON_MAPPER.writeValueAsString(obj));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			logger.error("转换Json错误", e);
		}
		return null;
	}
	
	public static String doPost(String url, Map<String, String> headers, Object obj) {
		try {
			return doPost(url, headers, JSON_MAPPER.writeValueAsString(obj));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			logger.error("转换Json错误", e);
		}
		return null;
	}
	
	public static String doPost(String apiUrl, String json) {
        return doPost(apiUrl, Collections.emptyMap(), json);
    }
	
    public static String doPost(String apiUrl, Map<String, String> headers, String json) {
 
        HttpPost httpPost = new HttpPost(apiUrl);
 
        // *) 配置请求headers
        if ( headers != null && headers.size() > 0 ) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
 
        // *) 配置请求参数
        if ( json != null) {
            StringEntity stringEntity = new StringEntity(json, "UTF-8");// 解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
        }
 
 
        CloseableHttpResponse response = null;
        try {
			response = httpClient.execute(httpPost);
			if (response == null || response.getStatusLine() == null) {
				return null;
			}

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity, "UTF-8");
			}
            return null;
        } catch (IOException e) {
        	logger.error("doPost", e);
        	e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
 
    }
 
	public String doPost(String apiUrl, Map<String, String> headers, Map<String, Object> params) {

		HttpPost httpPost = new HttpPost(apiUrl);

		// *) 配置请求headers
		if (headers != null && headers.size() > 0) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpPost.setHeader(entry.getKey(), entry.getValue());
			}
		}

		// *) 配置请求参数
		if (params != null && params.size() > 0) {
			HttpEntity entityReq = getUrlEncodedFormEntity(params);
			httpPost.setEntity(entityReq);
		}

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			if (response == null || response.getStatusLine() == null) {
				return null;
			}

			HttpEntity entityRes = response.getEntity();
			if (entityRes != null) {
				return EntityUtils.toString(entityRes, "UTF-8");
			}
			return null;
		} catch (IOException e) {
			logger.error("doPost", e);
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		return null;

	}
    
    private static HttpEntity getUrlEncodedFormEntity(Map<String, Object> params) {
        List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
            pairList.add(pair);
        }
        return new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8"));
    }
 
    private static String getUrlWithParams(String url, Map<String, Object> params) {
        boolean first = true;
        StringBuilder sb = new StringBuilder(url);
        for (String key : params.keySet()) {
            char ch = '&';
            if (first == true) {
                ch = '?';
                first = false;
            }
            String value = params.get(key).toString();
            try {
                String sval = URLEncoder.encode(value, "UTF-8");
                sb.append(ch).append(key).append("=").append(sval);
            } catch (UnsupportedEncodingException e) {
            }
        }
        return sb.toString();
    }
 
 
    public void shutdown() {
        idleThread.shutdown();
    }
 
    // 监控有异常的链接
    private static class IdleConnectionMonitorThread extends Thread {
 
        private final HttpClientConnectionManager connMgr;
        private volatile boolean exitFlag = false;
 
        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            this.connMgr = connMgr;
            setDaemon(true);
        }
 
        @Override
        public void run() {
            while (!this.exitFlag) {
                synchronized (this) {
                    try {
                        this.wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 关闭失效的连接
                this.connMgr.closeExpiredConnections();
                // 可选的, 关闭30秒内不活动的连接
                this.connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
            }
        }
 
        public void shutdown() {
            this.exitFlag = true;
            synchronized (this) {
                notify();
            }
        }
 
    }
	
	public static void download(String url, String savePath, Map<String, String> headers) {
		OutputStream out = null;
		InputStream in = null;
		CloseableHttpResponse response = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			// *) 配置请求headers
			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpGet.setHeader(entry.getKey(), entry.getValue());
				}
			}
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				long length = entity.getContentLength();
				if (length <= 0) {
					return;
				}
				in = entity.getContent();
				File file = new File(savePath);
				File parentFile = file.getParentFile();
				if(!parentFile.exists())
					parentFile.mkdirs();
				
				if (!file.exists()) {
					file.createNewFile();
				}
				out = new FileOutputStream(file);
				byte[] buffer = new byte[4096];
				int len = -1;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();

				if (out != null)
					out.close();

				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static byte[] download(String url, Map<String, String> headers) {
		CloseableHttpResponse response = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			// *) 配置请求headers
			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpGet.setHeader(entry.getKey(), entry.getValue());
				}
			}
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				long length = entity.getContentLength();
				if (length <= 0) {
					return null;
				}
				byte[] data = null;
	            if (entity != null){
	            	data = EntityUtils.toByteArray(entity);
	            }
	            return data;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 50; i++) {
//			System.out.println(HttpUtil.doPost("http://www.baidu.com"));
			HttpUtil.doPost("http://www.baidu.com");
			Thread.sleep(1000);
		}
		
	}
}