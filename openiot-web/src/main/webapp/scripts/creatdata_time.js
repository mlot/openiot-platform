/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

function getdata(d){
	var time = new Date();
	
	if(d.length>200){
		d.shift();
	}
	
	var previous = d.length ? d[d.length - 1] : 0;
	
	var y = (previous[1] ? previous[1] : 50) + Math.random() * 10 - 5;
	d.push([time,y < 0 ? 0 : y > 100 ? 100 : y]);

	return d;
}

function randomdata(d){
	for (var i=0; i<200; i++){
		var previous = d.length ? d[d.length - 1] : 0;
		var y = (previous[1] ? previous[1] : 50) + Math.random() * 10 - 5;
		d.push([(new Date()-(200-i)*100),0]);
	}
	return d;
}
