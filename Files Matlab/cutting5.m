clc
clear all
close all

% syms a b c m2 m3 m4 ag2x ag2y ag3x ag3y ag4x ag4y Ia2 Ia3 Ia4 teta2 teta3 teta4 alpha2 alpha3 alpha4 Fpx Fpy F12x F12y F32x F32y F43x F43y F32x F32y F14x F14y F43x F43y R12x R12y R32x R32y R43x R43y R23x R23y R14x R14y R34x R34y T12   

L1=0.06;%m
h1=0.02;
r1=0.005;
r12=0.005;
L2=0.15;%m
h2=0.02;
r2=0.005;
L3=0.02;
h3=0.10;
c13=0.02;
c23=0.02;
r3=0.005;
e=0.005;

den=1300; %density carbo fiber)

V1=e*(L1*h1+pi*r1*r1-2*r12*r12);
V2=e*(L2*h2+pi*r1*r1-2*r2*r2);
V3=e*(c13*c23+L3*h3-pi*r3*r3);

% a=0.005; %kg
% b=0.036; %kg
% c=0.027; %kg

m2=den*V1;
m3=den*V2;
m4=den*V3;

W2=-30;%rad/s vitesse angulaire moteur

%ag2=-L1*W2*W2*(cosd(teta2)+j*sind(teta2));
%ag3=-L2*W3*W3*(cosd(teta3)+j*sind(teta3));


Fpx=-20;%N
Fpy=0;

Ig2=0.000017;
Ig3=0.000091;
Ig4=0.000075;

for teta2=0:30:360
    disp(teta2)
    teta3=asind(L1*sind(teta2)/L2)+180;
teta4=0;
L3=L1*cosd(teta2)-L2*cosd(teta3);

W3=(L1/L2)*(cosd(teta2)/cosd(teta3))*W2; %vitesse angulaire link3
L3t=-L1*W2*sind(teta2)+L2*W3*sind(teta3); %vitesse lineaire slider cm/s

alpha2=0;%rad/sec2 acceleration angulaire
alpha3=(L1*alpha2*cosd(teta2)-L1*W2*W2*sind(teta2)+L2*W3*W3*sind(teta3))/(L2*cosd(teta3));
%alpha4=275;

L3tt=-L1*alpha2*sind(teta2)-L1*W2*W2*cosd(teta2)+L2*alpha3*sind(teta3)+L2*W3*W3*cosd(teta3); %mm/s2 acceleration lineaire slider

ag2x=-L1*alpha2*sind(teta2)-L1*W2*W2*cosd(teta2);
ag2y=L1*alpha2*cosd(teta2)-L1*W2*W2*sind(teta2);
ag3x=-L2*alpha3*sind(teta3)-L2*W3*W3*cosd(teta3);
ag3y=L2*alpha3*cosd(teta3)-L2*W3*W3*sind(teta3);
ag4x=L3tt;
ag4y=0;

R12=L1/2;
R32=L1/2;
R23=L2/2;
R43=L2/2;

R12x=R12*cosd(180+teta2);
R12y=R12*sind(180+teta2);
R32x=R32*cosd(teta2);
R32y=R32*sind(teta2);
R23x=R23*cosd(180+teta3);
R23y=R23*sind(180+teta3);
R43x=R43*cosd(teta3);
R43y=R43*sind(teta3);


A=[1 0 1 0 0 0 0 0;
    0 1 0 1 0 0 0 0;
    -R12y R12x -R32y R32x 0 0 0 1;
    0 0 -1 0 1 0 0 0;
    0 0 0 -1 0 1 0 0;
    0 0 R23y -R23x -R43y R43x  0 0;
    0 0 0 0 -1 0 0 0;
    0 0 0 0 0 -1 1 0];

B=[m2*ag2x;
    m2*ag2y;
    Ig2*alpha2;
    m3*ag3x;
    m3*ag3y;
    Ig3*alpha3;
    m4*ag4x-Fpx;
    -Fpy];
C=linsolve(A,B)
p=C(8,1);

end

%for loop iteration theta 2 de 0 a 360 deg avec pas 30 (pour trouver pour quelle valeur de theta 2 
%torque max)


