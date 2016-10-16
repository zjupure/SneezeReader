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

    filterAD();
}

function filterAD(){		
	var comment1 = "请各位河蟹评论";
	var comment2 = "请各位和谐评论";
	var comment3 = "道理你懂的";

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

		if(p.childNodes.length == 1){

            var img = p.getElementsByTagName("img");
            if(img != null){
                continue;
            }

			var text = p.innerHTML;
			if(text.match(comment1) || text.match(comment2)){
				target = i;
			}
		}



		var link = p.getElementsByTagName("a");
		if(link == null || link.length != 1){
			continue;
		}
		var href = link[0].getAttribute("href");
		if(href != null && ContainKeywords(href)){
			p.innerHTML = "";

			if(i >= 1){
				paragraph[i-1].innerHTML = "";
			}
			if(i+1 < n){
				paragraph[i+1].innerHTML = "";
			}
		}
	}


	for(var i = target - 1; i >= 0 && i >= target - 20; i--){

		var p = paragraph[i];

		if(p.childNodes.length != 1){

			continue;
		}

		var text = p.innerHTML;

		if(text == "" || (text.length == 1 && isSpace(text[0]))){

			if(p.parentNode != null){
                 p.innerHTML = "";
				/*p.parentNode.removeChild(p);*/
			}
			continue;
		}

		text = text.replace("&nbsp;", "");
		var isempty = true;

		for(var n = text.length, j = 0; j < n; j++){

			if(!isSpace(text[j]) && text[j] != "\""){
				isempty = false;
			}
		}

		if(isempty && p.parentNode != null){
		    p.innerHTML = "";
			/*p.parentNode.removeChild(p);*/
		}
	}


	var adscript = document.getElementsByTagName("script");
	for(var n = adscript.length, i = 0; i < n; i++){

		var src = adscript[i].getAttribute("src");

		if(src != null && src.match("adsbygoogle")){

			adscript[i].removeAttribute("src");
			adscript[i].innerHTML = "";
		}

		var text = adscript[i].innerHTML;
		if(text.match("adsbygoogle")){

			adscript[i].innerHTML = "";
		}
	}

	var adins = document.getElementsByClassName("adsbygoogle");
	for(var n = adins.length, i = 0; i < n; i++){

		var style = adins[i].getAttribute("style");

		if(style != null){
			adins[i].removeAttribute("style");
		}
	}
}


function isSpace(char){
	var emptyChar = new Array(" ", "\t", "\n", "\r", "\f", "\v");

	for(var i = 0, n = emptyChar.length; i < n; i++){

		if(char.match(emptyChar[i])){
			return true;
		}
	}

	return false;
}


function ContainKeywords(str){
	var keywords = new Array("sale", "product", "jd", "3mmr", "google", "show_ads",
		"taobao", "tmall", "mogujie", "weidian", "360buy", "baidu");
	for(var i = 0, n = keywords.length; i < n; i++){
		if(str.match(keywords[i])){
			return true;
		}
	}

	return false;
}