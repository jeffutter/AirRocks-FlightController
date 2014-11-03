#!/usr/bin/env node
/*
*	this is a nodejs server for testing and tuning ARFC PID values
*	:requires
*		SocketIO
*
*	command scheme: "name:value"
*
*		roll       RP:#  RI:#  RD:#	
*		pitch      PP:#  PI:#  PD:#
*		yaw        YP:#  YI:#  YD:#
*		throttle   T:#
*/
var app 
  , io
  , sys   = require('sys')
  , utils = require('./utils.js')
  , Driver= require('./ProcessDriver.js').PD
  , Test  = require('./ProcessDriver.js').PT
  , echo  = utils.echo
  , send  = utils.send
  , lock  = false
  , cycle = 1000
  ,	PATH  = "/home/arfc/arfc/flightController/"
  , EXEC  = "ARFCapp.o"
  ;

function run() {
	server = utils.CreateServer( requestCB );
	io     = utils.CreateIO( server );

	echo( server.address() );
	function requestCB( req, res ) {
		var p = utils.shortpath(req)
		switch( p ) {
			case 'test_results':
				return sendJSON(res, Test,PATH,EXEC,"-test")
			default:
				return send( req, res )
		}
	}

	io.sockets.on('connection', function (socket) {
		if( lock ) return false
		echo( 'connected' )
		lock = true;
		
		var pd = new Driver(PATH,EXEC, function(out) {
			echo(out);
			socket.emit("msg",out);
		}, function() {
			pd.clear();
			pd.start();
		});

		setInterval(function(){
			socket.emit('heartbeat',cycle)
		}, cycle); // 5s 

		socket.on('start', function(cmd) {
			pd.start();
		});

		socket.on('cmd', function(cmd) {
			echo( cmd )
			pd.send(cmd)
		});

		socket.on('update', function(msg) {
			echo( msg )
			pd.send(msg.data)
		});

		socket.on('disconnect',function() {
			echo('disconnected')
			lock   = false;
			pd.kill();
		});

	});

};

run();
















