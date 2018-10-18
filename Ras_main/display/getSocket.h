/*
This is header and source file about socker communication with raspberry pi sub.
*/

#ifndef GETSOCKET_H
#define GETSOCKET_H

// For Thread
#include <QThread> 

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

// For socket
#include <arpa/inet.h> 
#include <sys/types.h>
#include <sys/socket.h>
#include <string.h>

// For using data
#include <QString>

// Buffer size getting through socket
#define  BUFF_SIZE   1024

using namespace::std;

class getsocket : public QThread
{
	Q_OBJECT
public:
    // Constructor
	getsocket() : QThread(){}
	// Fuction running in this thread.
	void run()
	{
	    
	    // Variables for socket communication
		int   server_socket;
		int   client_socket;
		int   client_addr_size;
		struct sockaddr_in   server_addr;
		struct sockaddr_in   client_addr;
		char   buff_rcv[BUFF_SIZE+5];
		char   buff_snd[BUFF_SIZE+5];
		
		// Connecting socket
		server_socket  = socket( PF_INET, SOCK_STREAM, 0);
		if( -1 == server_socket)
		{
			printf( "server socket failed\n");
			exit( 1);
		}
		memset( &server_addr, 0, sizeof( server_addr));
		server_addr.sin_family     = AF_INET;
		server_addr.sin_port       = htons( 4000);
		server_addr.sin_addr.s_addr= htonl( INADDR_ANY);
		if( -1 == bind( server_socket, (struct sockaddr*)&server_addr, sizeof( server_addr) ) )
		{
			printf( "bind() execute error \n");
			exit( 1);
		}
		if( -1 == listen(server_socket, 5))
		{
			printf( "listen() execute failed\n");
			exit(1);
		}
		
		// Getting data from socket
		while(1)
		{
			client_addr_size  = sizeof( client_addr);
			client_socket     = accept( server_socket, (struct sockaddr*)&client_addr, &client_addr_size);
			if ( -1 == client_socket)
			{	
				printf( "confirm of client is failed\n");
				exit( 1);
			}
			read ( client_socket, buff_rcv, BUFF_SIZE);
			QString temp = QString(buff_rcv);
			setGesture(temp); // sending to main with getting data
			close(client_socket);
		}
	};
	// Function for call signal function(gesture).
	void setGesture(QString input_gesture)
	{
		emit gesture(input_gesture);
	}
signals:
    // For sending to main 
	void gesture(QString);
};


#endif // GETSOCKET_H


