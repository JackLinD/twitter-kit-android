# Android SDK TweetComposer Kit changelog

*Non-trivial pull requests should include an entry below. Entries must be suitable for inclusion in public-facing materials such as release notes and blog posts. Keep them short, sweet, and in the past tense. New entries go on top. When merging to deploy, add the version number and date.*

## Unreleased

## v0.7.4

* (DH) Removed tweet-composer dependency on twitter-core.

## v0.7.3
**Jan 30 2015**

* (EF) Removed targetSdkVersion because it should not be specified on libraries.

## v0.7.2
**Nov 20 2014**

* (TS) Moved to Java 7

## v0.7.1
**Oct 30 2014**

* (TY) Removed Apache 2.0 License from pom files.

## v0.7.0
**Oct 15 2014**

* (LTM) Removed allowBackup=true attribute from application element in AndroidManifest.xml.
* Create TweetComposer Builder to assist in building intent for Twitter for Android and will fallback to a Browser
