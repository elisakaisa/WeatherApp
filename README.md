# WeatherApp


## Description

This is an android application, in java, for getiing the weather forecast for all of Sweden using 
[SMHI](https://opendata.smhi.se/apidocs/metfcst/index.html)'s open api.

The application allows the user to enter any city/town/known place in Sweden and get the weather forecast for 10 days,
including temperature, preciptation type, precipitation amount, wind and a image illusrating the weather.

This application was done as a first project in the course "Mobile application and data mining" 
([CM2001](https://www.kth.se/student/kurser/kurs/CM2001?l=en)) at KTH in the fall 2021.
The application has since then been extended quite a bit and optimised.

## Installation

The easiest way to run the application is using the IDE Android Studio.

    git clone https://github.com/elisakaisa/WeatherApp.git

Then run gradle sync.

## Implementation

The application uses volley to handle request to the api. 
The application follows an MVVM structure (switch from chaos to MVVM in progress during the fall 2022)
The data is serilaized, allowing the user to view old data even without internet.

## Known issues and future improvements

Issues
- [#2](https://github.com/elisakaisa/WeatherApp/issues/2) the temperature text color is wrong

Improvements
- letting the user add favourite cities
- umproving UI

## Authors

Elisa Perini [github](https://github.com/elisakaisa) | [linkedIn](https://www.linkedin.com/in/elisa-perini-2759ba227/)
