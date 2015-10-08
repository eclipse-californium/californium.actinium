/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Martin Lanter
 ******************************************************************************/
/*
 * App to evaluate Rhino in AppServer
 * Quicksort source: http://en.literateprograms.org/Quicksort_%28JavaScript%29#chunk%20def:partition
 * JS = 10*Java: http://www.pankaj-k.net/spubs/articles/beanshell_rhino_and_java_perf_comparison/index.html
 *
 * Send a POST request with the payload
 * 		algr [input] [tests]
 *	algr: [fib, quick, newton]
 *	input: input for the algorithm (Default depends on algr).
 *	tests: How many tests should be done (Default is 10)
 * 		The fastest test counts.
 *
 * fib = Recursive Fibonacci algorithm
 * quick = Quicksort
 * newton = Newton's square root algorithm
 *
 * Examples:
 * 	fib 20 5
 * 		Computes 5 times the 20. fibonacci number recursively
 *
 * 	quick 10000
 * 		Sorts 10000 numbers 10 times with Quicksort
 *
 * 	newton
 * 		Computes 10 times all square roots from 1 to 999.999
 */ 

var measurements = 100;
var n_fib = 20;
var n_quick = 10000;
var n_newton = 1000000;

app.root.onget = function(request) {
	request.accept();
	
	var what = request.requestText;
	var text = '';
	
	app.dump('Fibonacci');
	run('fib 20'); // discart start-up
	text += 'fib\t20\t' + run('fib 20') + '\n';
	text += 'fib\t21\t' + run('fib 21') + '\n';
	text += 'fib\t22\t' + run('fib 22') + '\n';
	text += 'fib\t23\t' + run('fib 23') + '\n';
	text += 'fib\t24\t' + run('fib 24') + '\n';
	text += 'fib\t25\t' + run('fib 25') + '\n';
	text += 'fib\t26\t' + run('fib 26') + '\n';
	text += 'fib\t27\t' + run('fib 27') + '\n';
	text += 'fib\t28\t' + run('fib 28') + '\n';
	text += 'fib\t29\t' + run('fib 29') + '\n';
	text += 'fib\t30\t' + run('fib 30') + '\n';
	text += 'fib\t31\t' + run('fib 31') + '\n';
	text += 'fib\t32\t' + run('fib 32') + '\n';
	text += 'fib\t33\t' + run('fib 33') + '\n';
	text += 'fib\t34\t' + run('fib 34') + '\n';
	text += 'fib\t35\t' + run('fib 35') + '\n';

	app.dump('Quicksort');
	run('quick 5000'); // discart start-up
	text += 'quick\t5E3\t' + run('quick 5000') + '\n';
	text += 'quick\t1E4\t' + run('quick 10000') + '\n';
	text += 'quick\t2E4\t' + run('quick 20000') + '\n';
	text += 'quick\t3E4\t' + run('quick 30000') + '\n';
	text += 'quick\t4E4\t' + run('quick 40000') + '\n';
	text += 'quick\t5E4\t' + run('quick 50000') + '\n';
	text += 'quick\t6E4\t' + run('quick 60000') + '\n';
	text += 'quick\t7E4\t' + run('quick 70000') + '\n';
	text += 'quick\t8E4\t' + run('quick 80000') + '\n';
	text += 'quick\t9E4\t' + run('quick 90000') + '\n';
	text += 'quick\t1E5\t' + run('quick 100000') + '\n';
	text += 'quick\t2E5\t' + run('quick 200000') + '\n';
	text += 'quick\t3E5\t' + run('quick 300000') + '\n';
	text += 'quick\t4E5\t' + run('quick 400000') + '\n';
	text += 'quick\t5E5\t' + run('quick 500000') + '\n';

	app.dump('Newton');
	run('newton 1000'); // discart start-up
	text += 'newton\t1E3\t' + run('newton 1000') + '\n';
	text += 'newton\t1E4\t' + run('newton 10000') + '\n';
	text += 'newton\t1E5\t' + run('newton 100000') + '\n';
	text += 'newton\t1E6\t' + run('newton 1000000') + '\n';
	text += 'newton\t2E6\t' + run('newton 2000000') + '\n';
	text += 'newton\t3E6\t' + run('newton 3000000') + '\n';
	text += 'newton\t4E6\t' + run('newton 4000000') + '\n';
	text += 'newton\t5E6\t' + run('newton 5000000') + '\n';

	app.dump('DONE');
	
	app.dump(text.length);
	
	request.respond(69, text);
}

function run(what) {
	
	var prep = null; // preparation function
	var func; // function to mesaure
	var arg;
	var m = measurements;
	
	parts = what.split(" ");
	if (parts.length>0 && parts[0]=="fib") {
		func = fibonacci;
		arg = n_fib;
	} else if (parts.length>0 && parts[0]=="quick") {
		func = quick_sort;
		arg = n_quick;
		prep = prep_quick_sort;
	} else if (parts.length>0 && parts[0]=="newton") {
		func = newton_sqareroot;
		arg = n_newton;
	} else {
		request.respond(4.00, "unknown function");
		return;
	}
	
	if (parts.length>1) {
		arg = parseInt(parts[1]);
	}
	
	if (parts.length>2) {
		m = parseInt(parts[2]);
	}
	
	var dts = measure(func,prep,arg,m);
	var text = respond_measurement(dts);

	app.dump(parts[0], ''+arg, text);
	
	return text;
}

function respond_measurement(dts) {
	var avg = 0;
	var min = Number.MAX_VALUE;
	var max = 0;
	
	for (var i=0;i<dts.length;i++) {
		avg += dts[i];

		if (dts[i]<min) min = dts[i];
		if (dts[i]>max) max = dts[i];
	}
	avg = avg/dts.length;
	
	//stdev
	var stdev = 0;
	for (var i=0;i<dts.length;i++) {
		stdev += Math.pow(dts[i]-avg, 2);
	}
	
	//var text = 'Minimum = ' + (min/1000000) + 'ms, Maximum = ' + (max/1000000) + 'ms, Average = ' + (avg/1000000) + 'ms, Std.Deviation = ' + Math.sqrt(stdev/dts.length)/1000000;
	var text = '' + (min/1000000) + '\t' + (max/1000000) + '\t' + (avg/1000000) + '\t' + Math.sqrt(stdev/dts.length)/1000000;
	
	return text;
}

function measure(func, prep, arg, m) {
	var dts = new Array();
	for (var i=0;i<m;i++) {
	
		var arg_temp;
		if (prep!=null) {
			arg_temp = prep(arg);
		} else {
			arg_temp = arg;
		}
	
		var t0 = app.getNanoTime();
		
		func(arg_temp);
		
		var dt = app.getNanoTime()-t0;
		dts[dts.length] = dt;
		
		//app.dump("dt: "+(dt/1000000)+" ms");
	}
	return dts;
}

function fibonacci(n) {
	if (n<=1) return 1;
	else return fibonacci(n-1) + fibonacci(n-2);
}

function prep_quick_sort(size) {
	var array = new Array();
	for (var i=0;i<size; i++) {
		array.push(Math.random());
	}
	return array;
}

function quick_sort(array) {
	qsort(array, 0, array.length);
}

function qsort(array, begin, end) {
	if(end-1>begin) {
		var pivot=begin+Math.floor((end-begin)/2);

		pivot=partition(array, begin, end, pivot);

		qsort(array, begin, pivot);
		qsort(array, pivot+1, end);
	}
}

function partition(array, begin, end, pivot) {
	var piv=array[pivot];
	temp = array[pivot];
	array[pivot] = array[end-1];
	array[end-1] = temp;
	var store=begin;
	var ix, temp;
	for(ix=begin; ix<end-1; ++ix) {
		if(array[ix]<=piv) {
			temp = array[store];
			array[store] = array[ix];
			array[ix] = temp;
			++store;
		}
	}
	temp = array[store];
	array[store] = array[end-1];
	array[end-1] = temp;

	return store;
}

function newton_sqareroot(count) {
	/*var x = 10;
	var number;*/
	for (var j=1;j<count;j++) {
		var x = 10;
		var number = j;
		for (var i=0;i<8;i++) {
			x = x - (x*x - number)/(2*x);
		}
	}
}
