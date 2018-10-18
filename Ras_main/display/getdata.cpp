/*
This is source file about getting data from arduino.
*/
// Header file
#include "getdata.h"

// For QT framework
#include <QThread>
#include <QStringList>

// For IPC between qt process and sound sensing process
#include <fcntl.h>
#include <stdio.h>
#include <sys/stat.h>
#include <unistd.h>
#include <stdlib.h>

// Fuction that making circle data from input data.
void getdata::setCircle(QString input_data)
{
	QStringList datalist = input_data.split("/");
	if(datalist.length() <= 2)
		return;
	int datas[datalist.length()]={0};
	for(int i = 0 ; i<datalist.length();i++)
        	datas[i] = datalist[i].toInt();
	do_make(datas[0],datas[1], datas[2]);
}
// Function calling signal 'make'
void getdata::do_make(int _x, int _y, int _r)
{
	emit make(_x,_y,_r);
}

