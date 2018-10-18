/*
This is header file about making circle aniamation.
*/

#ifndef CIRCLEWIDGET_H
#define CIRCLEWIDGET_H

#include <QWidget>
#include <iostream>
#include <QThread>
#include <thread>
using namespace::std;
class CircleWidget : public QWidget
{
    Q_OBJECT
public:

    CircleWidget(QWidget *parent = 0);
    void setFloatBased(bool floatBased);
    void setAntialiased(bool antialiased);
    QSize minimumSizeHint() const Q_DECL_OVERRIDE;
    QSize sizeHint() const Q_DECL_OVERRIDE;
    // Setting points

    void setPointAndRadius(int _x, int _y, int _r);

    // Points that circle and radius.
    int x;
    int y;
    int r;
public slots:
    // Function for animation (Slots)
    void nextAnimationFrame();
signals:
    // Signal Function for closing.
    void finished();
protected:
    void paintEvent(QPaintEvent *event) Q_DECL_OVERRIDE;

private:
    bool floatBased;
    bool antialiased;
    int frameNo;
};
#endif // CIRCLEWIDGET_H
