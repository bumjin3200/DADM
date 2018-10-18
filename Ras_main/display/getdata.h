/*
This is header file that getting data from arduino
*/

#ifndef GETDATA_H
#define GETDATA_H

// For QT framework
#include <QThread>
#include <QString>

// For IPC between qt process and sound sensing process
#include <fcntl.h>
#include <stdio.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdlib.h>

using namespace::std;

class getdata : public QThread
{
    Q_OBJECT
public:
    // Constructor
    getdata():QThread(){}
    // Thread running
    void run()
    {
        myfifo = "/tmp/myfifo";
        while(1){
            fflush(stdin);
            fd = open(myfifo, O_RDONLY);
            read(fd,buf,1024);
	        if(buf!=""){
 	        QString input = QString(buf);
	        if(input.length() < 5 || input.length() >12)
    	    	continue;
 	        setCircle(input);
            }
        }
        close(fd);
    }
    int fd;
    char *myfifo;
    char buf[1024];
    int x;
    int y;
    int r;
	
	// Function that making circle data from input data.
	void setCircle(QString input_data);
	// Function calling signal 'make'
	void do_make(int _x,int _y,int _r);
signals:
    // Signal function for making circle.
    void make(int,int,int);
};
#endif // GETDATA_H
