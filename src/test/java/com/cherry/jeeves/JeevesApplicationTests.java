package com.cherry.jeeves;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class JeevesApplicationTests {
	public static void main(String[] args) throws Exception {
		System.out.println(new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT 0800 (中国标准时间)'", Locale.US).format(new Timestamp(1547881622339l)));
		ObjectMapper xmlMapper = new XmlMapper();
		String xml = "&lt;msg&gt;&lt;emoji fromusername = 'xxx' tousername = 'xxx' type='2' idbuffer='media:0_0' md5='4847d08c8c7a9f66b1b55a64b4cacb54' len = '388763' productid='' androidmd5='4847d08c8c7a9f66b1b55a64b4cacb54' androidlen='388763' s60v3md5 = '4847d08c8c7a9f66b1b55a64b4cacb54' s60v3len='388763' s60v5md5 = '4847d08c8c7a9f66b1b55a64b4cacb54' s60v5len='388763' cdnurl = 'http://emoji.qpic.cn/wx_emoji/mtTy8HSqIahIZnAmcvkWVX6N4fdqrN5oRN5owNTcibp6MqDxcZbYicRg/' designerid = '' thumburl = '' encrypturl = 'http://emoji.qpic.cn/wx_emoji/pdZUHSHl7RJiaWZib4oKTpY7ltwglLOeFAvicpqA72Fa0Kmic7QLLPPJOw/' aeskey= 'e7ab2e41b3e6b13833746a8ffc541783' externurl = 'http://emoji.qpic.cn/wx_emoji/JC9KWOk8B2MoB4w7c9mKtkE7K3GTEqZDBrUlHU1xCicv0mIDcumfOUMBt3rTBdtmO/' externmd5 = 'e5a51fcc8577b65b606d542963b9f791' width= '220' height= '220' tpurl= '' tpauthkey= '' attachedtext= '' attachedtextcolor= '' lensid= '' &gt;&lt;/emoji&gt; &lt;/msg&gt;";
		xml = xml.replace("<br/>", "").replace("&lt;", "<").replace("&gt;", ">").replace("&#39;", "'").replace("&quot;", "\"").replace("&amp;", "&");
		System.out.println(xml);
		Map<String, Map<String, String>> xmlMap = xmlMapper.readValue(xml, new TypeReference<Map<String, Map<String, String>>>() {});
		ObjectMapper jsonMapper = new ObjectMapper();
		System.out.println(xmlMap);
		System.out.println(jsonMapper.writeValueAsString(xmlMap));
		System.out.println(System.currentTimeMillis() + String.valueOf((int)(Math.random() * 1e4)));
		Pattern pattern = Pattern.compile("window.userAvatar\\s?=\\s?'data:img/jpg;base64,(.*)';");
		Matcher matcher = pattern.matcher("window.userAvatar = 'data:img/jpg;base64,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';");
		if (matcher.find())
			System.out.println(matcher.group(1));
		
		String headUrl = "/cgi-bin/mmwebwx-bin/webwxgeticon?seq=664173328&username=@6a4622796635b5f055e06e56b910381a&skey=@crypt_1bdac1b9_d1165124dfd11704410deee2a2a53677";
		String sub = headUrl.substring(headUrl.indexOf("seq=")+4);
		System.out.println(sub.substring(0, sub.indexOf("&")));
	}
}