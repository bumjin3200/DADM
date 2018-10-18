#ifndef WINDOW_H
#define WINDOW_H
// For QT framework
#include <QWidget>
#include <QString>
#include <QStringList>
#include <QTimer>

QT_BEGIN_NAMESPACE
class QLabel;
QT_END_NAMESPACE
class CircleWidget;



class Window : public QWidget
{
    Q_OBJECT
public:
    Window();
    // Constructor
    void makeCircle(int x, int y, int r);
    // In left sight, Function that sound to vision.
    void displayBackGroundAndroid(QString state);
    // In right sight, Function that show background imege.
    void displayStateAndroid(QString state, QString number, QString content);
    // In right sight, Function that show state, ex) calling, receiving msg.
    void displayAndroid(QString state, QString number, QString content);
    // Function that execute displayStateAndroid and displayBackGroundAndroid.
    void timerEvent(QTimerEvent *event);
public slots:
    void make(int x,int y,int r){
        makeCircle(x,y,r);
    }
    void gesture(QString ges){
	if(ges == "g1")//Normal bg
	{
		emit androidFinish();
	}
	else if(ges[1] == '2') //Call bg
	{
		emit androidFinish();
		QString temp = ges;
		QStringList  listTemp = temp.split("/");	
        	displayAndroid("call",listTemp[1],"");
	}
	else if(ges[1] == '3') //Msg bg
        {
		emit androidFinish();
		QString temp = ges;
		QStringList listTemp = temp.split("/");
		displayAndroid("msg",listTemp[1],listTemp[2]);
	}
    else if(ges == "g4") //Success bg
	{ 
		emit androidFinish();
    		displayBackGroundAndroid("success");
                startTimer(5000);
        }
	else if(ges == "g5") //Emergency bg
	{
		emit androidFinish();
		displayBackGroundAndroid("emergency");
	}
	
        
    }

signals:
    void androidFinish();
    // Signal Function for close display about android.
    void successFinish();
    // Signal Function for close display about success.
private:
    void paintEvent(QPaintEvent *e);
    // Fuction that set background imege
    QString stateAndroid;
    // State of android
};

#endif // WINDOW_H
