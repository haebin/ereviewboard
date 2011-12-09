ReviewBoard connector for Mylyn
===============================

This is a fork of the [well forked connector](https://github.com/rombert/ereviewboard).

Status
------

The connector maps all of the current attributes and allows for patches to be downloaded.
There is basic support for editing data:

1. Closing and reopening review requests;
1. Posting and updating diffs for selected [SCM integrations](ereviewboard/wiki/SCM-Integrations);
1. Post-commit review request support. You can select committed revision(s) to request a review.;
1. You can select unmodified files to request a review for pre-commit review.;
1. Autocomplete for reviewer and group. After selecting the name, names will be converted to IDs.;

Please see [the project wiki](ereviewboard/wiki) for more details.

Roadmap
-------

The development plans are reflected in the [issue tracker](ereviewboard/issues).

Dowloads
--------

* Archived update sites are periodically posted in the [downloads section](ereviewboard/archives/master)
* A p2 update site is available at [http://rombert.github.com/ereviewboard/update/](http://rombert.github.com/ereviewboard/update/).

Builds
----------

* Not applied yet.