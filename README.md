[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/_Od3b_Hk)

# Keep in mind when you run the code

- Set the location of your emulator to a location in Berlin. Otherwise, the app will not work
  properly.
- The app is optimized for the Pixel 6 with API 34. The app should also work on other devices, but
  the layout might be a bit off.
- The app is optimized for the English language. The app should also work in German. Change the
  language in the settings of your emulator.

# Documentation

## Idea of the App

The app helps you to find a toilet in Berlin. You can save a toilet as a favorite and you can also
rate a toilet. For each toilet you can see more details in a bottom sheet. You can also see the
location
of the toilet on a map.

## Development Process

The majority of our project was completed over a single weekend, beginning with the development of
the map.
Concurrently, we worked on the favorite list and integrated markers on the map. We then enhanced
this
functionality by dynamically loading toilets based on the user's current view, ensuring only
relevant
toilets are displayed. Subsequently, we developed a bottom sheet detailing toilet information and
added a
feature to rate the toilets. All these functionalities are accessible offline, but we also
synchronize data
with Firebase (Firestore) for online integration. Additionally, we introduced a settings page that
allows users
to adjust the default zoom level and the app's theme. Throughout this process, we made significant
improvements
to existing features and addressed several bugs.

## Validation & Testing

## Validation of the Idea

We validated our idea by asking some friends and co-students if they would use an app that helps
them to find a toilet in Berlin and which features they would like to have in such an app.
After we got positive feedback, we started with the creation of the first wireframes. We again asked
for feedback and made some changes to the wireframes. One Change was for example the addition of the
icons to the not extended version of the bottom sheet, to see some information about the toilet
without extending the bottom sheet.
After gathering feedback for the wireframes, we started with the implementation of the app.

## Testing during the Development Process

During the development process, we tested our features most of the time on the emulated version of
the Pixel 6 with API 34.
But we also installed it on a real Pixel 7 to see if everything works as expected.

## Code Validation

Next to the known process of code reviews on GitHub, we also used SonarCloud to check our code for
bugs and code smells.
This means that before a pull request was merged, it was automatically checked by SonarCloud and the
results were posted in the pull request and only if the results were good, we were able to merge the
pull request.

## Final User Testing

After the implementation of all features, we asked some friends to test the app and give us
feedback.
After this feedback, we made some changes to the app. For example, we adapted some translations
because they were misleading to the testers.

## Team Work

Different components of the project were tackled individually by our team. Christian focused mainly
on the map and the entire backend, including elements like WcEntities, Favorites, and Rating. He
utilized
Firebase and Room Database for data persistence and was responsible for the initial implementation
of the
bottom sheet. On the other hand, Julius concentrated on the frontend aspect. His responsibilities
included
designing the favorites list, the bottom sheet, and the settings page. Additionally, he was in
charge of
implementing SharedPreferences and managing the localization of the project.

# Design

## Problem

We're addressing the challenge of locating clean toilets in Berlin, a task currently fraught with
difficulty.
Berlin has a large number of public toilets, but they're often difficult to find and not always in
the best
condition. This is especially problematic for tourists, who may not be familiar with the city and
are
therefore unable to locate nearby toilets. Additionally, the current system of locating toilets is
inefficient,
as it requires users to manually search for toilets online or through other means. This is a
time-consuming
process that could be significantly improved through the use of a dedicated app.

## Proposed Solution

Our app provides a map displaying all toilets in Berlin, allowing users to easily find a nearby
toilet.
Users have the option to add toilets to their favorites list and rate them. Additionally, more
information
about each toilet is available in a bottom sheet, enhancing the ease of finding a suitable toilet.

## Navigation

Our app features three primary interfaces: the map, favorites list, and settings page. The map
serves as the
initial screen, and users can switch to the favorites list or settings page through the navigation
drawer.
We opted for this straightforward navigation layout as it's user-friendly and well-suited for
addressing
our specific problem, without the need for a more complicated structure.

# Used Tools

- GitHub CoPilot
- ChatGPT 4
