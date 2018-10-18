/*
This is sourece file about making circle animation. 
*/

#include "circlewidget.h"
#include <QPainter>
#include <stdlib.h>

// Circlewidget Constructor 
CircleWidget::CircleWidget(QWidget *parent): QWidget(parent)
{
    floatBased = false;
    antialiased = false;
    frameNo = 0;
    setAntialiased(true);
    setFloatBased(true);
    setBackgroundRole(QPalette::Base);
    setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Expanding);
}
// Function about float Base
void CircleWidget::setFloatBased(bool floatBased)
{
    this->floatBased = floatBased;
    update();
}
// Function setting Antialiased 
void CircleWidget::setAntialiased(bool antialiased)
{
    this->antialiased = antialiased;
    update();
}
// Function getting minimum size hint.
QSize CircleWidget::minimumSizeHint() const
{
    return QSize(50, 50);
}
// Function getting minimum size
QSize CircleWidget::sizeHint() const
{
    return QSize(180, 180);
}
// Function about circle animation. At each cycle, frameNo is increased. If frameNo's value is 127, This class is closed.
void CircleWidget::nextAnimationFrame()
{
        if(frameNo != 127) // 127 is maximum frame
        {
            ++frameNo;
            update(); // proceeding to next frame
        }
        else{
            close();
            finished();
        }
}
// Function setting circle points and radius.
void CircleWidget::setPointAndRadius(int _x, int _y, int _r){
    x =_x;
    y =_y;
    r =_r;
}
// Function about drawing circle.
void CircleWidget::paintEvent(QPaintEvent *)
{
    QPainter painter(this);
    painter.setRenderHint(QPainter::Antialiasing, antialiased);
    painter.translate(x, y); // Setting circle points

    // Making circle
    for (int diameter = 0; diameter < r; diameter += 9) {
        int delta = abs((frameNo % 128) - diameter / 2);
        int alpha = 255 -(delta * delta) / 4 - diameter;
        if (alpha > 0) {
            painter.setPen(QPen(QColor(200, diameter / 2, 0, alpha), 3));
        if (floatBased)
            painter.drawEllipse(QRectF(-diameter / 2.0, -diameter / 2.0, diameter, diameter));
        else
            painter.drawEllipse(QRect(-diameter / 2, -diameter / 2, diameter, diameter));
        }
    }
}
