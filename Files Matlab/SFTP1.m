clc
clear all
close all

%

sigmay=255;%N/mm2 (stainless)
N=1.4; %(saftey fact)
s=(2*(sigmay^2))/N

r1=0.005*1000;%mm
h1=0.02*1000;
e=0.005*1000;
A1=pi*(r1^2);
A2=(h1-(2*r1))*e;

F12=sqrt(28.99^2);
F32=sqrt(25.19^2 + 3.2^2);
F43=sqrt(20.2^2 + 11.3^2);
F14=sqrt(12.2^2);

sigma12=F12/A1;
sigma32=F32/A1;
sigma43=F43/A1;
sigma14=F14/A1;

sigma212=F12/A2;
sigma232=F32/A2;
sigma243=F43/A2;
sigma214=F14/A2;

s12=2*((sigma12)^2)
s32=2*((sigma32)^2)
s43=2*((sigma43)^2)
s14=2*((sigma14)^2)

s212=2*((sigma212)^2)
s232=2*((sigma232)^2)
s243=2*((sigma243)^2)
s214=2*((sigma214)^2)

%comparasion entre sigma et eq chap 4 