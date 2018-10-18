/*
This is main source file in DADM
*/
// Header file making by user
#include "window.h"
#include "getdata.h"
#include "getSocket.h"
// For QT Framework
#include <QApplication>
#include <QThread>
#include <QTimer>

using namespace::std;
//Additional header files

int main(int argc, char *argv[])
{
    QApplication app(argc, argv);
    Window *window; // window pointer
    getdata *gd; //getdate pointer
    getsocket *gs; // getSocket pointer
    gd = new getdata();
    gs = new getsocket();
    window = new Window();
   
    window->show(); //window showing
    window->resize(800,480); // window size setting
    gd->start(); 
    QObject::connect(gd,SIGNAL(make(int,int,int)),window, SLOT(make(int,int,int))); // making circle
    gs->start();
    QObject::connect(gs,SIGNAL(gesture(QString)),window, SLOT(gesture(QString))); // state of other raspberry pi
    return app.exec();
}

