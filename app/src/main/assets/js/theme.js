function setTheme(theme){
	var style = document.createElement("style");
	style.setAttribute("type", "text/css");

	var dayCss = "body{background-color:#ffffff;color:#000000}\na:link{color:#0000ff;}";
	var nightCss = "body{background-color:#3b3b3b;color:#ffffff}\na:link{color:#0066ff;}";
	var cssText = nightCss;

	if(theme == 'day'){
		cssText = dayCss;
	}else if(theme == 'night'){
		cssText = nightCss;
	}

	var textNode = document.createTextNode(cssText);
	style.appendChild(textNode);

	head = document.head || document.getElementsByTagName("head")[0];
	html = document.html || document.getElementsByTagName("html")[0];
	if(head != null){
		head.appendChild(style);
	}else if(html != null){
		html.appendChild(style);
	}else{
		document.appendChild(style);
	}
}					


function changeTheme(cssPath){
	var fileref = document.createElement("link");
	fileref.setAttribute("rel", "stylesheet");
	fileref.setAttribute("type", "text/css");
	fileref.setAttribute("href", cssPath);

	head = document.head || document.getElementsByTagName("head")[0];
	html = document.html || document.getElementsByTagName("html")[0];
	if(head != null){
		head.appendChild(fileref);
	}else if(html != null){
		html.appendChild(fileref);
	}else{
		document.appendChild(fileref);
	}
}


function filterAD(){		
	var content = "友情提示：请各位河蟹评论。道理你懂的";
	var target = -1;
	var reg = "<hr>广告<br><script.*>.*</script><hr><br>";
	var pattern = new RegExp(reg, "g");
	
	var html = document.html || document.getElementsByTagName("html")[0];
	var result = null;
	do{
		result = pattern.exec(html.innerHTML);
		if(result != null){
			var res = html.innerHTML.replace(result, "");
			html.innerHTML = res;
		}
	}while(result != null);
	
	var paragraph = document.getElementsByTagName("p");
	for(var n = paragraph.length, i = 0; i < n; i++){
		var p = paragraph[i];

		if(p.childNodes.length == 1 && p.firstChild.nodeValue.match(content)){
			target = i - 2;
			if(target >= 1 && paragraph[target].getElementsByTagName("a") != null){
				paragraph[target].innerHTML = "";
			}
		}
		
		var link = p.getElementsByTagName("a");
		if(link == null || link.length != 1){
			continue;
		}
		var href = link[0].getAttribute("href");
		if(href != null && ContainKeywords(href)){
			p.innerHTML = "";
		}		
	}
}


function ContainKeywords(str){
	var keywords = new Array("sale", "product", "jd", "3mmr", "google", "show_ads", 
		"taobao", "tmall", "mogujie", "weidian");
	for(var i = 0, n = keywords.length; i < n; i++){
		if(str.match(keywords[i])){
			return true;
		}
	}

	return false;
}