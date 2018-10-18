/*
This is display class for making circles and show state.
*/
// User defined Header files.
#include "circlewidget.h"
#include "window.h"
// For QT framework 
#include <QtWidgets>
#include <QImage>
#include <QTimer>
// ETC
#include <string>
#include <QDebug>
using namespace::std;

Window::Window()
{
    setWindowTitle(tr("DADM"));
    // Making title message
    stateAndroid = "none";
    // Initializing state of Android
}
void Window::displayAndroid(QString state, QString number, QString content)
{
    stateAndroid = state;
    // Changing state of Android
    displayBackGroundAndroid(state);
    // Setting background of right sight
    displayStateAndroid(state,number,content);
    // Setting Message of right sight
}
void Window::displayStateAndroid(QString state,QString number,QString content)
{
    // Setting String
    QString *words;
    QFont font;
    if(state == "call") // When user's smart phone receives calling.
    {
        words = new QString("Call from \n\n" + number); // Result of calling and phone number
        font.setPointSize(32);// Size setting
    }
    else if(state == "msg") // When user's smart phone receives message.
    {
	    for(int i = 16 ; i<content.length() ; i = i+17) // For insert '\n'
	    	content.insert(i,"\n");
        words = new QString("from " + number+"\n" + content); // Result of message and phone number
	    font.setPointSize(20); // Size setting
    }
    // Setting Text Color
    QPalette palette;
    QBrush brush(QColor(255, 255, 255, 255)); 
    brush.setStyle(Qt::SolidPattern);
    palette.setBrush(QPalette::Active, QPalette::WindowText, brush);
    palette.setBrush(QPalette::Inactive, QPalette::WindowText, brush);

    // Making widget
    QLabel *displayMsg = new QLabel(this);
    displayMsg->setFont(font);
    displayMsg->setText(*words);
    displayMsg->setPalette(palette);
    displayMsg->setAlignment(Qt::AlignCenter);

    // Display widget
    displayMsg->move(455,200);         //QLabel위치 조정
    displayMsg->show();

    // Connect function for closing this.
    connect(this, SIGNAL(androidFinish()),displayMsg, SLOT(close()));

}

void Window::displayBackGroundAndroid(QString state)
{
    // Setting imege
    QImage *img;
    QLabel *imgview;
    img = new QImage();
    QPixmap *buffer = new QPixmap();
    // When calling
    if(state == "call")
          img->load(":/call.png");
    // When msg is received
    else if(state == "msg")
          img->load(":/msg.png");
    // When reply msg sending is success
    else if(state == "success")
          img->load(":/success.png");
    // When emergency state
    else if(state == "emergency")
	  img->load(":/emergency.png");
	 // IMG setting
    *buffer = QPixmap::fromImage(*img);

    // Making widget
    imgview = new QLabel(this);
    imgview->setPixmap(*buffer);
    imgview->resize(buffer->width(),buffer->height());

    // Display widget
    if(state != "emergency") // Because of different size
	    imgview->move(440,47);
    imgview->show();

    // Connect function for closing this.
    if(state != "success") // Ending function for not success display 
	    connect(this, SIGNAL(androidFinish()),imgview, SLOT(close()));
    else // Ending function for Success display 
	    connect(this, SIGNAL(successFinish()),imgview, SLOT(close()));
}
void Window::makeCircle(int x, int y, int r)
{
    // For connect function
    QTimer *timer = new QTimer(this);

    // Setting and making CircleWidget
    CircleWidget *circleWidgets;
    QGridLayout *layout = new QGridLayout();
    circleWidgets = new CircleWidget();
    circleWidgets->resize(400,480);

    // Points that made circle
    circleWidgets->setPointAndRadius(x,y,r);

    // Setting timer for circle display
    timer->start(12);
    connect(timer, SIGNAL(timeout()),circleWidgets, SLOT(nextAnimationFrame()));
    layout->addWidget(circleWidgets,1,1);
    setLayout(layout);
    delete layout; 
}
// Background img setting
void Window::paintEvent(QPaintEvent *e)
{
    QPainter painter(this);
    painter.drawPixmap(0, 0, QPixmap(":/normal.png").scaled(size()));
    QWidget::paintEvent(e);
}
// Ending success display 
void Window::timerEvent(QTimerEvent *event)
{
        emit successFinish();
        stateAndroid = "none";
}
