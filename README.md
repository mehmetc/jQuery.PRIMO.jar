#jQuery.PRIMO (companion library)

- This is the server part of [jQuery.PRIMO](https://github.com/mehmetc/jQuery.PRIMO). Go there for more info
- Put this library in: /exlibris/primo/p4_1/ng/primo/home/system/tomcat/search/webapps/primo_library#libweb/WEB_INF/lib
- It will create a Rest endpoint to session and record data. 
    - base endpoint: http://yourlimo.installation/primo_library/libweb/jqp
```bash
 $ fe_web
 $ cd WEB_INF/lib
```

#Compiling
If you want to recompile you will notice that jQuery.PRIMO.Helpers.java is missing. Ping me and I will send it to you. 


# Available calls
##Get library version
```http
    /version
```

```string
    1.0.0
```    

## Get your session information
```http
    /session
```

**NOT LOGGED IN**
```json
    {
        "view": {
            "institution": {
                "code": "KUL",
                "name": "KU Leuven"
            },
            "code": "KULeuven",
            "interfaceLanguage": "en_US"
        },
        "ip": {
            "institution": {},
            "address": "123.123.123.1"
        },
        "sessionId": "5D1EB953B1220EF3F3B5B5C355D137A1",
        "user": {
            "name": "anonymous",
            "isLoggedIn": false,
            "ranking": {
                "categories": null,
                "prefer_new": null
            },
            "id": "anonymous-5D1EB953B1220EF3F3B5B5C355D137A1",
            "email": null,
            "isOnCampus": false
        },
        "pds": {"url": null}
    }
```

**LOGGED IN**
```json
{
    "view": {
        "institution": {
            "code": "KUL",
            "name": "KU Leuven"
        },
        "code": "KULeuven",
        "interfaceLanguage": "en_US"
    },
    "ip": {
        "institution": {},
        "address": "123.123.123.1"
    },
    "sessionId": "5D1EB953B1220EF3F3B5B5C355D137A1",
    "user": {
        "name": "Celik, Mehmet",
        "isLoggedIn": true,
        "ranking": {
            "categories": "engineering",
            "prefer_new": "1"
        },
        "id": "u0060010",
        "email": "mehmet.celik@libis.kuleuven.be",
        "isOnCampus": false
    },
    "pds": {
        "handle": "1111111111111111111111111111111",
        "url": null
    }
```

## Resolve a deduped record id
```http
    /record/resolve/dedupmrg105612753
```    

```json
    ["MYRECORD_99123456789","MYRECORD_99123456799"]
```

## Get record data for an id from your result set
It will perform a search if it can not find your data on the result set
### Get as PNX
```http
    /record/32LIBIS_ALMA_DS71124679060001471.pnx
```
### Get original record
```http    
    /record/32LIBIS_ALMA_DS71124679060001471.xml
```
### Get as JSON
```http    
    /record/32LIBIS_ALMA_DS71124679060001471.json
```
### Get all records as JSON. 
This will return what is set in the result set of a pre-run search query. The result set is volatile this means
that when you click on ex. details the result set will only contain that single record.
```http    
    /record/*.json
```

## Get facet data from result set
It will get the facets from the last result set. 
### Get as JSON
```http
    /facets
```
