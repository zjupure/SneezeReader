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