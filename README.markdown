URI Templates
=============

This library provides easy expansion of URI templates.

It is a partial implementation of RFC 6570, found at
http://code.google.com/p/uri-templates/

So far, the library only supports simple String values, and
no modifiers on the variable specs.

Examples
--------

Given the template:

   http://www.google.com/{?q}

and the context q=nalle, the result will be

  http://www.google.com/?q=nalle


Credits
-------

This library is a product of the Cloudname Open Source factory.

The algorithm used is based on appendix A of the RFC.

