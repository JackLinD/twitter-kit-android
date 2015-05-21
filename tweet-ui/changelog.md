# Android SDK TweetUi Kit changelog

*Non-trivial pull requests should include an entry below. Entries must be suitable for inclusion in public-facing materials such as release notes and blog posts. Keep them short, sweet, and in the past tense. New entries go on top. When merging to deploy, add the version number and date.*

## Unreleased

* (DH) Fixed bug in SearchTimeline preventing some filter queries from being used.
* (DH) Removed cobalt performance-metrics dependency.
* (DH) Added result_type=filtered to SearchTimeline queries to use new backend search workflow.
* (DH) Improved TweetView display with reduced full name font weight on newer API versions.

## v1.2.0

* (DH) Added CollectionTimeline to show Tweets from a Twitter collection.
* (DH) Added 'next' support to the TimelineListAdapter
* (DH) Removed final from TimelineCursor class.
* (AP) Updated Picasso dependency version to 2.5.2, OkHttp required to be 2.0 or greater.

## v1.1.0

* (AP) (internal) Added timeline impression scribing.
* (DH) Added UserTimeline to show Tweets for a particular user.
* (DH) Added SearchTimeline to show Tweets that match a search query.
* (DH) Added TweetTimelineListAdapter for providing ListViews with a scrollable Timeline of Tweets.
* (AP) Added retweet display support to show the original Tweet with retweeted by attribution.

## v1.0.7

* (DH) Fixed TweetView MetricsManager IllegalArgumentException bug.
* (DH) (internal) Added a UserTimeline.

## v1.0.6

* (AP) Fixed strict mode violations on startup when using OkHttp
* (DH) Update performance-metrics to 0.2.0
* (DH) (internal) Added foundation for timelines with a TimelineListAdapter and SearchTimeline, an implementation of Timeline.
* (DH) Switched to using com.twitter.cobalt:performance-metrics for performance monitoring.

## v1.0.5
**Jan 30 2015**

* (DH) Changed package for AppSession used internally

## v1.0.4
**Jan 29 2015**

* (EF) Removed targetSdkVersion because it should not be specified on libraries.

## v1.0.3
**Dec 15 2014**

* (DH) Fixed TweetViewAdapter to set an empty Tweet list when null is passed to setTweets
* (TS) Added Consumer Proguard Config file to be bundled with AAR
* (DH) Stopped Share Tweet button from showing in all caps on API 21

## v1.0.2
**Nov 20 2014**
* (AP) Scribe SyndicatedSdkImpression type events on Tweet view impressions and permalink clicks
* (TS) Moved to Java 7
* (AP) Fixed scribing requests dropping User-Agent and X-Client-UUID headers
* (DH) Fixed exception in TweetViewAdapter.
* (DH) Started treating sessions with tokens known to be expired so new tokens are fetched upfront.
* (DH) Improved TweetView and CompactTweetView layout preview in IDEs to show an example Tweet.

## v1.0.1
**Oct 30 2014**

* (TY) Removed Apache 2.0 License from pom files.

## v1.0.0
**Oct 15 2014**

* (AP) Updated support library version 21.0.0
* (LTM) Removed allowBackup=true attribute from application element in AndroidManifest.xml.
* (YH) Changed the loading of Active Session from TweetCore to be in background.
* (AP) Configured to scribe to talk to https://syndication.twitter.com/i/jot/sdk
* (AP) Added required ScribeConfig arguments for scribe path components
* (LTM) Fixed scribe bug where events were not sent to the backend whenever TweetUi was using a user session for making API calls.
* (LTM) Introduced AppSession for holding app tokens. TwitterSession now holds only user tokens.
* (LTM) Switched to using new SessionManager that manages the app sessions. Updated TwitterApiClient to accept Session instead of TwitterSession.
* (DH) Disabled setting Tweet fields in Tweet views rendered in edit mode (e.g. for IDE layout preview)
* (DH) Disabled the Tweet view and logged the error when views are created, but the TweetUi kit hasn't been started in Fabric.with()
* (DH) Added debug logging for single Tweet load failures, error logging for kit or auth related errors.
* (LTM) Configured scribe using analytics settings, which is used by crashlytics for configuring Answers. This is temporary until we have can get our scribe settings from the backend. If analytics settings are not available, fallback to defaults.
* (AP) Added SyndicationScribeEvent for product metrics
* (AP) Added many languages for localization
* (AP) Updated ScribeFilesSender to send device id header
* (DH) Fixed bug in BaseTweetView.setTweet in which tapping a Tweet took the user to the previous Tweet
* (DH) Removed TweetView and CompactTweetView OnTwitterExceptionListener (breaking change)
* (AP) Updated ScribeConfig with configurable User-Agent
* (DH) Added loadTweet and loadTweets to handle guest auth and request queueing
* (DH) Added TweetViewFetcherAdapter.setTweetIds that accepts a LoadCallback
* (DH) Made TweetViewFetchAdapter returns Tweets in requested order, no longer optional (breaking change)
* (DH) Removed TweetViewFetchAdapter's setOnTwitterExceptionListener (breaking change)
* (AP) Improved accessibility for users of TalkBack
* (LTM) Updated Twitter API Tweet model to be immutable model.
* (LTM) Updated Twitter API User model to be immutable model.
* (LTM) Updated Twitter API entity models to be immutable models.
* (DH) Removed TweetView and CompactTweetView setOnTwitterExceptionListener and loadTweet (breaking change)
* (DH) Removed TweetView and CompactTweetView constructors which accepted Tweet ids (breaking change)
* (DH) Changed Share Tweet button margin slightly

## v0.2.1 (Beta2.1 Irvine)

* (AP) Fixed html entities in Tweets passed in via StatusesService
* (DH) Renamed TweetView and CompactTweetView style attributes to allow creating custom styles and themes
* (AP) Added color calculations for secondary colored visual elements
* (DH) Added photo to CompactTweetView with 16:9 aspect ratio and center cropped
* (DH) Removed XML "tw__preload_(name, screen_name, text, timestamp)" attributes from Tweet views (breaking change)

## v0.2.0 (Beta2 Halifax)
** Sep 15 2014**

* (TS) Added fluent API builder to Fabric Class and cleaned up Fabric API
* (DH) Renamed twittersocial to tweetui
* (LTM) Renamed twittercore to core
* (TS) Renamed Foundation to Fabric
* (DH) Added "tw__" prefix to TweetView and CompactTweetView XML attributes (breaking change)
* (LTM) Updated SessionManager to support multiple sessions. This is a breaking API change.

## v0.1.1 (Beta1.1 Guerneville)
** Sep 4 2014**

* (DH) Fix TweetViewFetchAdapter out-of-order Tweets problem
* (AP) Added Share Tweet button to TweetViews
* (DH) Added U-specific avatar, media default, and error drawables.
* (DH) Added TweetView and CompactTweetView constructors taking a style to be applied to the view.
* (DH) Added R.style.tw__TweetLightStyle and R.style.tw__TweetDarkStyle styles which may be used or subclassed.
* (DH) Allowed style to be set by the standard XML 'style' attribute (e.g. style="@style/tw__TweetLightStyle")

## v0.1.0 (Beta1 Fremont)
**Aug 18 2014**

* (DH) Clear session when guest/app token expires
* (LTM) Reset version from 1.0.4 to 0.1.0
* (TS) Replaced Kit#getName with Kit#getIdentifier, fixed issue where kit names could be reported incorrectly after proguarding.
* (DH) Make the super class of TweetView and CompactTweetView, BaseTweetView, public
* (DH) Updated Tweet timestamps to show relative time format
* (DH) Removed share button and share listener (breaking change)
* (DH) Made TweetView and CompactTweetView design redline adjustments and cleaned up styles
* (AP) Added basic scribing with no de-duplication effort.
* (AP) Removed theme interface (breaking change)
* (AP) Added error listener to more explicit OnTwitterExceptionListener
* (AP) Added requirement to call AbstractTweetView.loadTweet to fetch tweet from the Twitter api for tweets constructed with an id (breaking change)
* (AP) Removed success listener from tweet view (breaking change)
* (AP) Fixed tweet entity indices in presence of html chars and emoji
* (DH) Changed TweetViewAdapter constructor to accept Tweets (breaking change, no longer accepts Builders).
* (DH) TweetViewFetchAdapter constructed by tweetIds and performs multiget statuses/lookup.
* (DH) Changed TweetView and CompactTweetView constructors to accept Tweet id or Tweet, Builder removed (breaking change).
* (TS) Replaced API stack with new Twittercore API
* (TS) Added Picasso for image loading

## v1.04 (Eureka)
**July 24 2014**

* (DH) Added TweetViewAdapter to support AdapterViews such as ListView
* (AP) Added setOnShareListener to TweetViewBuilder interface
* (AP) Added share action to compact and normal tweets
* (DH) Removed Tweet reply, retweet, and favorite actions and ComposerActivity
* (AP) Added onSuccess and onError callbacks to TweetView that can be assigned in the builder

## v1.0.3 (Denver)
**June 26 2014**

* (DH) Each TweetView class provides a Builder. Builder constructors now require a tweetId (breaking change). Removed method createCompact.
* (DH) Add support for setting preloaded data via XML.
* (DH) Improved Tweet compose transition animation, button disabling, remaining character warning.
* (AP) Added support for viewing tweets while logged-out
* (AP) Split TwitterSocial.setAuth into setAuthConfig and setAuthToken (breaking change)

## v1.0.2 (Chicago)
**June 3 2014**

* (AP) Added support for setting preloaded data, equivalent to data in oembed.
* (AP) Updated TweetView's default click event to launch permalink.
* (DH) Implemented tweet reply action button.
* (AP) Added best effort error recovering and error parsing.
* (LTM) Removed twitter auth permission since it was never explicitly required by twittersocial. twitteridentity removed this permission some time ago since it is no longer required for Twitter Single Sign On.
* (TS) Migrated app onboarding code from crashlytics to foundation.
* (IC) Added support for tracking rate limiting.
* (AP) Added support for character counting when composing a tweet.
* (DH) Implemented retweet/undo retweet action button.

## v1.0.1 (Boston)
**May 20 2014**

* (DH) Fixed tweet view inconsistencies.
* (LTM) Changed how version information is generated.
* (DH) Implemented favorite/unfavorite tweet action button.
* (TS) Renamed Module to Kit.
* (AP) Improved startup time by initializing syndication db connection asynchronously.

## v1.0.0-SNAPSHOT
**April 28 2014**

* Initial version
