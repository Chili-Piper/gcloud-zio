# Chilipiper's Google Cloud ZIO

This multiproject contains suite of wrapper libraries around Google Cloud instrastructure.

## Goal
Provide necessary required zio idiomatic functions for internal use (and thus it may be missing some), but any sensible contribution (whether it's more coverage of functionality, more documentation, tests, better typed errors...) is welcomed.

Projects are cross compiled/published for Scala 2.12 and 2.13 (and later for Dotty).


## Usage
Install the module you require (see below), and if needed check corresponding module's tests to see usage.
There are few real tests as most of the functionality is straightfoward or is integration-related. Most of them serve purely
for above-mentioned purpose.

## Modules

### [Google PubSub](https://cloud.google.com/pubsub)

```
resolvers += Resolver.bintrayRepo("chili-piper", "gcloud-zio"),

libraryDependencies += "com.chilipiper" %% "pubsub" % version
```

## Design goals

- **Strive to provide minimal, non-stringly typed api API.**
 Google SDK API has often many overloads. We do not try to mimic it one-to-one. Instead we aim to provide the reasonable and typed alternative.
 
- **Do not enforce any architectural style on clients, provide only thin wrapper.**
 There may be more options, some more zio-idiomatic, some less (i.e. with regards to usage of the R part of ZIO). We aim to not enforce any particular style on users.

- **No dependencies outside of ZIO and Gcloud SDK** 
 
- **Provide streaming alternative whenever streaming is viable**

