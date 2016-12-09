var getLibraryDefinitions = function() {
    //============================================================
    // Add library definitions here
    //============================================================

    var localCss = RP.stash.api.defineStashCss;
    var localLib = RP.stash.api.defineLocalLib;
    var inlineScript = RP.stash.api.defineInlineScript;
    var postLoadInlineScript = RP.stash.api.definePostLoadInlineScript;
    // Get files for the 8.1 library, it has logic to load the 
    // configured theme

    var get8_1Files = function(){
        var themeName = "jda-dark-theme", 
            files =  [
            // LABjs build 0 to tip
            localLib(true, "Deploy/3rdparty/labjs/lab.2.0.3.min.js", 0, undefined),
            localLib(false, "Deploy/3rdparty/labjs/lab.2.0.3.debug.js", 0, undefined),

            // Build 0 to tip uses Ext 4.1
            localLib(true, "Deploy/3rdparty/extjs/4.1/ext-all.js", 0, undefined, 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/4.1/ext-all-dev.js", 0, undefined, 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/4.1/\";", 0, undefined, 0, undefined),
            
            // loading straight up ext overrides before any other rp libraries are loaded
            localLib(true, "Deploy/8.1.{DROP}/rpcommon-overrides.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.1.{DROP}/rpcommon-overrides.{client-mode}{BUILD}.js", 0, undefined),

            // RP Library Dependencies
            localLib(true, "Deploy/8.1.{DROP}/rpcommon-core.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.1.{DROP}/rpcommon-core.debug{BUILD}.js", 0, undefined),

            inlineScript(undefined, "RP.stash.DEPLOYED_ROOT=\"{STASHROOT}Deploy/8.1.{DROP}/\";", 0, undefined),

            localCss(true, "Deploy/3rdparty/bootstrap/css/bootstrap-responsive.min.css", 0, undefined),
            localCss(false, "Deploy/3rdparty/bootstrap/css/bootstrap-responsive.css", 0, undefined),
            
            localLib(true, "Deploy/8.1.{DROP}/rpcommon.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.1.{DROP}/rpcommon.{client-mode}{BUILD}.js", 0, undefined),
            
            localLib(true, "Deploy/8.1.{DROP}/rpcommon-hydra.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.1.{DROP}/rpcommon-hydra.{client-mode}{BUILD}.js", 0, undefined)
            ];
          
            
            if (RP && RP.globals && RP.globals.CUSTOM_THEME_NAME) {
                themeName = RP.globals.CUSTOM_THEME_NAME;
            }

            files.push(
                localCss(true, "Deploy/8.1.{DROP}/" + themeName + ".min{BUILD}.css", 0, undefined), 
                localCss(false, "Deploy/8.1.{DROP}/" + themeName + ".debug{BUILD}.css", 0, undefined)
            );
            
          
          return files;
    };

    
    
    // DO NOT EDIT THE BUILD NUMBERS BELOW.  THEY ARE AUTOMATICALLY EDITED BY THE BUILD PROCESS
    // THAT AUTOMATICALLY BUMPS BUILD NUMBERS FOR THE "OfficialBuild" TYPE.  THE ONLY THING YOU
    // SHOULD DO IS TO ADD NEW ENTRIES FOR NEW VERSIONS OF A LIBRARY, OR TO ADD NEW LIBRARIES-
    // JUST BE SURE TO FOLLOW THE SAME PATTERN...
    // {BUMPBUILD_BEGIN}
    var BUILDN = {
        "rpcore": {
            "1.0": 0,
            "2.0": 0
        },

        "i18n": {
            "1.0": 0
        },
        
        "rpcommon": {
            "3.1": 15,
            "3.2": 17,
            "3.3": 118,
            "3.4": {
                "2": 0,
                "3": 0,
                "4": 4,
                "5": 5,
                "6": 15,
                "8": 17,
                "9": 16,
               "10": 9,
               "11": 11
            },
            "5.0": {
                "3": 0,
                "5": 0
            },
            "7.0": {
                "0": 4,
                "1": 2,
                "2": 3,
                "3": 2,
                "4": 0,
                "5": 6,
                "6": 0,
                "7": 0
            },
            "7.1": {
                "0": 16,
                "1": 15,
                "2": 9,
                "3": 8,
                "4": 2
            },
            "8.0": {
                "0": 9,
                "1": 7
            },
            "8.1": {
                "0": 1,
                "2": 9,
                "3": 8,
                "4": 2
            }
        },
        
        "csunit": {
            "3.3": 1,
            "5.0": 0,
            "7.0": 0,
            "7.1": 0,
            "8.0": 0,
			"8.1": 0
        }
    };
    // {BUMPBUILD_END}

    var getMaxDrop = function(library, version) {
        var versionObject = BUILDN[library][version];
        var maxDrop = -1;

        for (var drop in versionObject) {
            var dropNumber = parseInt(drop, 10);

            if (dropNumber > maxDrop) {
                maxDrop = dropNumber;
            }
        }

        return maxDrop;
    };

    return [{
        name: "rpcore",    // library name
        "1.0": {
            bmin: 0,
            bmax: BUILDN.rpcore["1.0"],
            files:
            [
            localLib(true, "Deploy/rpcore/1.0/rpcore.min.js", 0, undefined),
            localLib(false, "Deploy/rpcore/1.0/rpcore.debug.js", 0, undefined),
            localCss(true, "Deploy/rpcore/1.0/rpcore-css.min.css", 0, undefined),
            localCss(false, "Deploy/rpcore/1.0/rpcore-css.debug.css", 0, undefined)
            ]
        },
        "2.0": {
            bmin: 0,
            bmax: BUILDN.rpcore["2.0"],
            files:
            [
            localLib(true, "Deploy/rpcore/2.0/rpcore.min.js", 0, undefined),
            localLib(false, "Deploy/rpcore/2.0/rpcore.debug.js", 0, undefined),
            localCss(true, "Deploy/rpcore/2.0/rpcore-css.min.css", 0, undefined),
            localCss(false, "Deploy/rpcore/2.0/rpcore-css.debug.css", 0, undefined)
            ]
        }
    }, {
        name: "i18n",    // library name
        "1.0": {
            bmin: 0,
            bmax: BUILDN.rpcore["1.0"],
            files:
            [
            localLib(true, "Deploy/i18n/1.0/i18n.min.js", 0, undefined),
            localLib(false, "Deploy/i18n/1.0/i18n.debug.js", 0, undefined)
            ]
        }
    }, {
        name: "rpcommon",    // library name
        "3.1": {
            bmin: 5,        // min build number
            bmax: BUILDN.rpcommon["3.1"],   // max build number = current build #  [DO NOT CHANGE!]
            files:
            [
            localCss(undefined, "Deploy/3rdparty/extjs/3.1/ext-all.css", 0, undefined),
            localLib(true, "Deploy/3rdparty/extjs/3.1/ext-base-3-1-1.js", 5, undefined),
            localLib(true, "Deploy/3rdparty/extjs/3.1/ext-all-3-1-1.js", 5, undefined),
            localLib(false, "Deploy/3rdparty/extjs/3.1/ext-base-debug-3-1-1.js", 5, undefined),
            localLib(false, "Deploy/3rdparty/extjs/3.1/ext-all-debug-3-1-1.js", 5, undefined),
            inlineScript(undefined, "RP.stash.DEPLOYED_ROOT=\"{STASHROOT}Deploy/3.1/\";", 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/3.1/\";", 0, undefined),
            postLoadInlineScript(undefined, "Ext.BLANK_IMAGE_URL=\"{STASHROOT}Deploy/3rdparty/extjs/3.1/resources/images/default/s.gif\";\r\n", 0, undefined),
            localCss(undefined, "Deploy/3.1/resources/css/xtheme-RP{BUILD}.css", 0, 3),
            localCss(undefined, "Deploy/3.1/resources/css/xtheme-RP-004.css", 4, 8),
            localCss(undefined, "Deploy/3.1/resources/css/xtheme-RP-005.css", 9, undefined),
            localCss(undefined, "Deploy/3.1/rp-all{BUILD}.css", 0, undefined),
            localLib(true, "Deploy/3.1/RPExt.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/3.1/RPExt.debug{BUILD}.js", 0, undefined)
            ]
        },
        "3.2": {
            bmin: 1,        // min build number
            bmax: BUILDN.rpcommon["3.2"],   // max build number = current build #  [DO NOT CHANGE!]
            files:
            [
            // ExtJS Library Dependencies
            localCss(undefined, "Deploy/3rdparty/extjs/3.2/resources/css/ext-all.css", 0, undefined),
            localLib(true, "Deploy/3rdparty/extjs/3.2/ext-base.js", 0, undefined),
            localLib(true, "Deploy/3rdparty/extjs/3.2/ext-all.js", 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/3.2/ext-base-debug.js", 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/3.2/ext-all-debug.js", 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/3.2/\";", 0, undefined),

            // RP Library Dependencies
            localLib(true, "Deploy/3.2/rpcommon-core.min{BUILD}.js", 15, undefined),
            localLib(false, "Deploy/3.2/rpcommon-core.debug{BUILD}.js", 15, undefined),

            inlineScript(undefined, "RP.stash.DEPLOYED_ROOT=\"{STASHROOT}Deploy/3.2/\";", 0, undefined),
            postLoadInlineScript(undefined, "Ext.BLANK_IMAGE_URL=\"{STASHROOT}Deploy/3rdparty/extjs/3.2/resources/images/default/s.gif\";\r\n", 0, undefined),
            localCss(undefined, "Deploy/3.2/resources/css/xtheme-RP-001.css", 0, undefined),
            localCss(undefined, "Deploy/3.2/rp-all{BUILD}.css", 0, undefined),
            localLib(true, "Deploy/3.2/RPExt.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/3.2/RPExt.debug{BUILD}.js", 0, 14),
            localLib(false, "Deploy/3.2/RPExt.{client-mode}{BUILD}.js", 15, undefined)
            ]
        },
        "3.3": {
            bmin: 1,        // min build number
            bmax: BUILDN.rpcommon["3.3"],   // max build number = current build #  [DO NOT CHANGE!]
            files:
            [
            // ExtJS Library Dependencies
            localCss(undefined, "Deploy/3rdparty/extjs/3.3/resources/css/ext-all.css", 0, undefined),
            localLib(true, "Deploy/3rdparty/extjs/3.3/ext-base.js", 0, undefined),
            localLib(true, "Deploy/3rdparty/extjs/3.3/ext-all.js", 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/3.3/ext-base-debug.js", 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/3.3/ext-all-debug.js", 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/3.3/\";", 0, undefined),

            // RP Library Dependencies
            localLib(true, "Deploy/3.3/rpcommon-core.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/3.3/rpcommon-core.debug{BUILD}.js", 0, undefined),

            inlineScript(undefined, "RP.stash.DEPLOYED_ROOT=\"{STASHROOT}Deploy/3.3/\";", 0, undefined),
            postLoadInlineScript(undefined, "Ext.BLANK_IMAGE_URL=\"{STASHROOT}Deploy/3rdparty/extjs/3.3/resources/images/default/s.gif\";\r\n", 0, undefined),
            localCss(undefined, "Deploy/3.3/resources/css/xtheme-RP-001.css", 0, undefined),
            localCss(undefined, "Deploy/3.3/rp-all{BUILD}.css", 0, 12),
            localCss(true, "Deploy/3.3/rpcommon-css.min{BUILD}.css", 13, undefined),
            localCss(false, "Deploy/3.3/rpcommon-css.debug{BUILD}.css", 13, undefined),
            localLib(true, "Deploy/3.3/RPExt.min{BUILD}.js", 0, 12),
            localLib(false, "Deploy/3.3/RPExt.{client-mode}{BUILD}.js", 0, 12),
            localLib(true, "Deploy/3.3/rpcommon.min{BUILD}.js", 13, undefined),
            localLib(false, "Deploy/3.3/rpcommon.{client-mode}{BUILD}.js", 13, undefined)
            ]
        },
        "3.4": {
            dmin: 2,
            dmax: getMaxDrop("rpcommon", "3.4"),
            drops: {
                "2": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["3.4"]["2"]
                },
                "3": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["3.4"]["3"]
                },
                "4": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["3.4"]["4"]
                },
                "5": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["3.4"]["5"]
                },
                "6": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["3.4"]["6"]
                },
                "8": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["3.4"]["8"]
                },
                "9": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["3.4"]["9"]
                },
                "10": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["3.4"]["10"]
                },
                "11": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["3.4"]["11"]
                }

            },
            files:
            [
            // ExtJS Library Dependencies
            localCss(undefined, "Deploy/3rdparty/extjs/3.3/resources/css/ext-all.css", 0, undefined, 0, undefined),
            localLib(true, "Deploy/3rdparty/extjs/3.3/ext-base.js", 0, undefined, 0, undefined),
            localLib(true, "Deploy/3rdparty/extjs/3.3/ext-all.js", 0, undefined, 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/3.3/ext-base-debug.js", 0, undefined, 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/3.3/ext-all-debug.js", 0, undefined, 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/3.3/\";", 0, undefined, 0, undefined),

            // RP Library Dependencies
            localLib(true, "Deploy/3.4.{DROP}/rpcommon-core.min{BUILD}.js", 0, undefined, 0, undefined),
            localLib(false, "Deploy/3.4.{DROP}/rpcommon-core.debug{BUILD}.js", 0, undefined, 0, undefined),


            inlineScript(undefined, "RP.stash.DEPLOYED_ROOT=\"{STASHROOT}Deploy/3.4.{DROP}/\";", 0, undefined, 0, undefined),
            postLoadInlineScript(undefined, "Ext.BLANK_IMAGE_URL=\"{STASHROOT}Deploy/3rdparty/extjs/3.3/resources/images/default/s.gif\";\r\n", 0, undefined, 0, undefined),
            localCss(undefined, "Deploy/3.4.{DROP}/resources/css/xtheme-RP-001.css", 0, undefined, 0, undefined),
            localCss(true, "Deploy/3.4.{DROP}/rpcommon-css.min{BUILD}.css", 0, undefined, 0, undefined),
            localCss(false, "Deploy/3.4.{DROP}/rpcommon-css.debug{BUILD}.css", 0, undefined, 0, undefined),
            localLib(true, "Deploy/3.4.{DROP}/rpcommon.min{BUILD}.js", 0, undefined, 0, undefined),
            localLib(false, "Deploy/3.4.{DROP}/rpcommon.{client-mode}{BUILD}.js", 0, undefined, 0, undefined)

            ]
        },
        "5.0": {
            dmin: 3,
            dmax: getMaxDrop("rpcommon", "5.0"),
            drops: {
                "3": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["5.0"]["3"]
                },
                "5": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["5.0"]["5"]
                }
            },
            files:
            [
            // ExtJS Library Dependencies
            //localCss(undefined, "Deploy/3rdparty/extjs/4.0/resources/css/ext-all.css", 0, undefined, -- building our own from scratch for now
            localLib(true, "Deploy/3rdparty/extjs/4.0/ext-all.js", 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/4.0/ext-all-dev.js", 0, undefined),
            localLib(undefined, "Deploy/3rdparty/extjs/4.0/ext3-core-compat.js", 0, undefined),
            localLib(undefined, "Deploy/3rdparty/extjs/4.0/ext3-compat.js", 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/4.0/\";", 0, undefined),
            
            // loading straight up ext overrides before any other rp libraries are loaded
            localLib(true, "Deploy/5.0.{DROP}/rpcommon-overrides.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/5.0.{DROP}/rpcommon-overrides.{client-mode}{BUILD}.js", 0, undefined),

            // RP Library Dependencies
            localLib(true, "Deploy/5.0.{DROP}/rpcommon-core.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/5.0.{DROP}/rpcommon-core.debug{BUILD}.js", 0, undefined),

            inlineScript(undefined, "RP.stash.DEPLOYED_ROOT=\"{STASHROOT}Deploy/5.0.{DROP}/\";", 0, undefined),

            localCss(true, "Deploy/5.0.{DROP}/rpcommon-css.min{BUILD}.css", 0, undefined),
            localCss(false, "Deploy/5.0.{DROP}/rpcommon-css.debug{BUILD}.css", 0, undefined),
            
            localLib(true, "Deploy/5.0.{DROP}/rpcommon.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/5.0.{DROP}/rpcommon.{client-mode}{BUILD}.js", 0, undefined),

            localLib(true, "Deploy/5.0.{DROP}/compatibility-rpcommon.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/5.0.{DROP}/compatibility-rpcommon.{client-mode}{BUILD}.js", 0, undefined)
            ]
        },
        "7.0": {
            dmin: 0,
            dmax: getMaxDrop("rpcommon", "7.0"),
            drops: {
                "0": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.0"]["0"]
                },
                "1": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.0"]["1"]
                },
                "2": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.0"]["2"]
                },
                "3": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.0"]["3"]
                },
                "4": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.0"]["4"]
                },
                "5": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.0"]["5"]
                },
                "6": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.0"]["6"]
                },
                "7": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.0"]["7"]
                }
            },
            files:
            [
            // ExtJS Library Dependencies
            //localCss(undefined, "Deploy/3rdparty/extjs/4.0/resources/css/ext-all.css", 0, undefined, -- building our own from scratch for now
            
            // Build 0 to 2 uses 4.0
            localLib(true, "Deploy/3rdparty/extjs/4.0/ext-all.js", 0, 2, 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/4.0/ext-all-dev.js", 0, 2, 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/4.0/\";", 0, 2, 0, undefined),
            
            // Build 3 to tip uses 4.0.7
            localLib(true, "Deploy/3rdparty/extjs/4.0.7/ext-all.js", 3, undefined, 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/4.0.7/ext-all-dev.js", 3, undefined, 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/4.0.7/\";", 3, undefined, 0, undefined),
            postLoadInlineScript(undefined, "Ext.BLANK_IMAGE_URL = (Ext.isIE6 || Ext.isIE7) ? '{STASHROOT}Deploy/3rdparty/extjs/4.0.7/s.gif' : 'data:image/gif;base64,R0lGODlhAQABAID/AMDAwAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==';", 0, undefined, 0, undefined),
            
            // loading straight up ext overrides before any other rp libraries are loaded
            localLib(true, "Deploy/7.0.{DROP}/rpcommon-overrides.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/7.0.{DROP}/rpcommon-overrides.{client-mode}{BUILD}.js", 0, undefined),

            // RP Library Dependencies
            localLib(true, "Deploy/7.0.{DROP}/rpcommon-core.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/7.0.{DROP}/rpcommon-core.debug{BUILD}.js", 0, undefined),

            inlineScript(undefined, "RP.stash.DEPLOYED_ROOT=\"{STASHROOT}Deploy/7.0.{DROP}/\";", 0, undefined),

            localCss(true, "Deploy/7.0.{DROP}/rpcommon-css.min{BUILD}.css", 0, undefined),
            localCss(false, "Deploy/7.0.{DROP}/rpcommon-css.debug{BUILD}.css", 0, undefined),
            
            localLib(true, "Deploy/7.0.{DROP}/rpcommon.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/7.0.{DROP}/rpcommon.{client-mode}{BUILD}.js", 0, undefined)
            ]
        },
        "7.1": {
            dmin: 0,
            dmax: getMaxDrop("rpcommon", "7.1"),
            drops: {
                "0": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.1"]["0"]
                },
                "1": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.1"]["1"]
                },
                "2": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.1"]["2"]
                },
                "3": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.1"]["3"]
                },
                "4": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["7.1"]["4"]
                }
            },
            files:
            [
            // ExtJS Library Dependencies
            //localCss(undefined, "Deploy/3rdparty/extjs/4.1/resources/css/ext-all.css", 0, undefined, -- building our own from scratch for now
            localLib(true, "Deploy/3rdparty/labjs/lab.2.0.3.min.js", 0, undefined),
            localLib(false, "Deploy/3rdparty/labjs/lab.2.0.3.debug.js", 0, undefined),

            // Build 0 to tip uses Ext 4.1
            localLib(true, "Deploy/3rdparty/extjs/4.1/ext-all.js", 0, undefined, 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/4.1/ext-all-dev.js", 0, undefined, 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/4.1/\";", 0, undefined, 0, undefined),

            // loading straight up ext overrides before any other rp libraries are loaded
            localLib(true, "Deploy/7.1.{DROP}/rpcommon-overrides.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/7.1.{DROP}/rpcommon-overrides.{client-mode}{BUILD}.js", 0, undefined),

            // RP Library Dependencies
            localLib(true, "Deploy/7.1.{DROP}/rpcommon-core.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/7.1.{DROP}/rpcommon-core.debug{BUILD}.js", 0, undefined),

            inlineScript(undefined, "RP.stash.DEPLOYED_ROOT=\"{STASHROOT}Deploy/7.1.{DROP}/\";", 0, undefined),

            localCss(true, "Deploy/7.1.{DROP}/rpcommon-css.min{BUILD}.css", 0, undefined),
            localCss(false, "Deploy/7.1.{DROP}/rpcommon-css.debug{BUILD}.css", 0, undefined),
            
            localLib(true, "Deploy/7.1.{DROP}/rpcommon.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/7.1.{DROP}/rpcommon.{client-mode}{BUILD}.js", 0, undefined)
            ]
        },        
        "8.0": {
            dmin: 0,
            dmax: getMaxDrop("rpcommon", "8.0"),
            drops: {
                "0": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["8.0"]["0"]
                },
                "1": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["8.0"]["1"]
                }
            },
            files: [
            // LABjs build 0 to tip
            localLib(true, "Deploy/3rdparty/labjs/lab.2.0.3.min.js", 0, undefined),
            localLib(false, "Deploy/3rdparty/labjs/lab.2.0.3.debug.js", 0, undefined),

            // Build 0 to tip uses Ext 4.1
            localLib(true, "Deploy/3rdparty/extjs/4.1/ext-all.js", 0, undefined, 0, undefined),
            localLib(false, "Deploy/3rdparty/extjs/4.1/ext-all-dev.js", 0, undefined, 0, undefined),
            inlineScript(undefined, "RP.stash.EXTJS_DEPLOYED_ROOT=\"{STASHROOT}Deploy/3rdparty/extjs/4.1/\";", 0, undefined, 0, undefined),
            
            // loading straight up ext overrides before any other rp libraries are loaded
            localLib(true, "Deploy/8.0.{DROP}/rpcommon-overrides.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.0.{DROP}/rpcommon-overrides.{client-mode}{BUILD}.js", 0, undefined),

            // RP Library Dependencies
            localLib(true, "Deploy/8.0.{DROP}/rpcommon-core.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.0.{DROP}/rpcommon-core.debug{BUILD}.js", 0, undefined),

            inlineScript(undefined, "RP.stash.DEPLOYED_ROOT=\"{STASHROOT}Deploy/8.0.{DROP}/\";", 0, undefined),

            localCss(true, "Deploy/3rdparty/bootstrap/css/bootstrap-responsive.min.css", 0, undefined),
            localCss(false, "Deploy/3rdparty/bootstrap/css/bootstrap-responsive.css", 0, undefined),
            
            localLib(true, "Deploy/8.0.{DROP}/rpcommon.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.0.{DROP}/rpcommon.{client-mode}{BUILD}.js", 0, undefined),
            
            localLib(true, "Deploy/8.0.{DROP}/rpcommon-hydra.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.0.{DROP}/rpcommon-hydra.{client-mode}{BUILD}.js", 0, undefined),

            localCss(true, "Deploy/8.0.{DROP}/rpcommon-css.min{BUILD}.css", 0, undefined), 
            localCss(false, "Deploy/8.0.{DROP}/rpcommon-css.debug{BUILD}.css", 0, undefined)
            ]
        },
        "8.1": {
            dmin: 0,
            dmax: getMaxDrop("rpcommon", "8.1"),
            drops: {
                "0": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["8.1"]["0"]
                },
                "2": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["8.1"]["2"]
                },
                "3": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["8.1"]["3"]
                },
                "4": {
                    bmin: 0,
                    bmax: BUILDN.rpcommon["8.1"]["4"]
                }
            },
            files: get8_1Files()
        }
    },{
        name: "charting",    // library name
        "1.0": {
            bmin: 0,
            bmax: 0,
            files:
            [
            localLib(undefined, "Deploy/3rdparty/ejschart/2.1/excanvas.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ejschart/2.1/EJSChart.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ejschart/2.1/EJSChart_SVGExport.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ejschart/2.1/iepatch.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ejschart/2.1/moveLegendPatch.js", 0, 0),
            localCss(undefined, "Deploy/3rdparty/ejschart/2.1/EJSChartCustom.css", 0, undefined)
            ]
        }
    },{
        name: "csunit",    // library name
        "3.3": {
            bmin: 0,
            bmax: BUILDN.csunit["3.3"],
            files:
            [
            localLib(undefined, "Deploy/3rdparty/yui/yui/yui-min.js", 0, undefined),
            localLib(true, "Deploy/3.3/csunit.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/3.3/csunit.debug{BUILD}.js", 0, undefined)
            ]
        },
        "5.0": {
            bmin: 0,
            bmax: BUILDN.csunit["5.0"],
            files:
            [
            localLib(undefined, "Deploy/3rdparty/yui/yui/yui-min.js", 0, undefined),
            localLib(true, "Deploy/5.0/csunit.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/5.0/csunit.debug{BUILD}.js", 0, undefined)
            ]
        },
        "7.0": {
            bmin: 0,
            bmax: BUILDN.csunit["7.0"],
            files:
            [
            localLib(undefined, "Deploy/3rdparty/yui/yui/yui-min.js", 0, undefined),
            localLib(true, "Deploy/7.0/csunit.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/7.0/csunit.debug{BUILD}.js", 0, undefined)
            ]
        },
        "7.1": {
            bmin: 0,
            bmax: BUILDN.csunit["7.1"],
            files:
            [
            localLib(undefined, "Deploy/3rdparty/yui/yui/yui-min.js", 0, undefined),
            localLib(true, "Deploy/7.1/csunit.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/7.1/csunit.debug{BUILD}.js", 0, undefined)
            ]
        },
        "8.0": {
            bmin: 0,
            bmax: BUILDN.csunit["8.0"],
            files:
            [
            localLib(undefined, "Deploy/3rdparty/yui/yui/yui-min.js", 0, undefined),
            localLib(true, "Deploy/8.0/csunit.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.0/csunit.debug{BUILD}.js", 0, undefined)
            ]
        },
		"8.1": {
		    bmin: 0,
            bmax: BUILDN.csunit["8.1"],
            files:
            [
            localLib(undefined, "Deploy/3rdparty/yui/yui/yui-min.js", 0, undefined),
            localLib(true, "Deploy/8.1/csunit.min{BUILD}.js", 0, undefined),
            localLib(false, "Deploy/8.1/csunit.debug{BUILD}.js", 0, undefined)
            ]
		}
    },{
        name: "extplugins",
        "1.0": {
            bmin: 0,
            bmax: 0,
            files:
            [
            localLib(undefined, "Deploy/3rdparty/extjs/plugins/RowExpander.js", 0, 0)
            ]
        }
    },{
        name: "ux",
        "1.0": {
            bmin: 0,
            bmax: 0,
            files:
            [
            localLib(undefined, "Deploy/3rdparty/ux/scheduler/Ext.ux.AbstractScheduleSelectionModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/scheduler/Ext.ux.ScheduleActivitySelectionModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/scheduler/Ext.ux.ScheduleModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/scheduler/Ext.ux.SchedulePanel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/scheduler/Ext.ux.ScheduleRowSelectionModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/scheduler/Ext.ux.ScheduleView.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/scheduler/Ext.ux.TimelineModel.js", 0, 0),

            // new time bar.
            localLib(undefined, "Deploy/3rdparty/ux/sch/TimelineConstants.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractActivityContainer.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractRow.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractContainerRow.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractNavigationRow.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractTimelineHeader.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractActivityRow.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractActivity.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractLabel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractMoveConstraintModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/abstract/AbstractResizeConstraintModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Constraints/SimpleMoveConstraintModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Constraints/SimpleResizeConstraintModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Constraints/DisabledMoveConstraintModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Constraints/DisabledResizeConstraintModel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Synchronizer/Core.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Synchronizer/Navigation.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/SchedulePanel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/ScrollPanel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/LabelHeader.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/TimelineHeader.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/LabelsPanel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/LabelFooter.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/ToolTip.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Movable.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Resizable.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Overlay.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/ActivityPanel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/OverlayPanel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Navigation/ScrollBar.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Navigation/MapPanel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Navigation/NavigationPanel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Labels/SimpleLabel.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Activities/SimpleActivity.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/Activities/DragAndDropActivity.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/ActivityRows/SimpleRow.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/ActivityRows/DragAndDropRow.js", 0, 0),
            localLib(undefined, "Deploy/3rdparty/ux/sch/ActivityRows/OverlayRow.js", 0, 0),
            localCss(undefined, "Deploy/3rdparty/ux/scheduler/Ext.ux.Schedule.css", 0, undefined),
            localCss(undefined, "Deploy/3rdparty/ux/sch/rp-scheduler.css", 0, undefined)
            ]
        }
    },{
        name: "tinymce",
        "3.4": {
            bmin: 7,
            bmax: 7,
            files:
            [
            localLib(undefined, "Deploy/3rdparty/tinymce/3.4.7/tiny_mce.js", 7, 7),
            localLib(undefined, "Deploy/3rdparty/tinymce/3.4.7/Ext.ux.TinyMCE.js", 7, 7)
            ]
        },
        "3.5": {
            bmin: 8,
            bmax: 8,
            files:
            [
            localLib(undefined, "Deploy/3rdparty/tinymce/3.5.8/tiny_mce.js", 8, 8),
            localLib(undefined, "Deploy/3rdparty/tinymce/3.5.8/Ext.ux.TinyMCE.js", 8, 8)
            ]
        }
    }
    /**
     ,{ // TEST ONLY!  THIS IS FOR DEBUGGING PURPOSES ONLY!!!
     name: "bogus",    // library name
     "3.0":           // include files for a specific version; prefix ':' in front of version #
     {
     bmin: 0,          // min build number
     bmax: 10,         // max build number
     files:
     [
     localCss(undefined, "Deploy/3.0/bogus.css", 0, undefined),

     localLib(true, "Deploy/3.0/between-0-and-5.js", 0, 5),
     localLib(true, "Deploy/3.0/between-6-and-7.js", 6, 7),
     localLib(true, "Deploy/3.0/the-tip.js", 8, undefined),

     localLib(false, "Deploy/3.0/between-0-and-5-debug.js", 0, 5),
     localLib(false, "Deploy/3.0/between-6-and-7-debug.js", 6, 7),
     localLib(false, "Deploy/3.0/the-tip-debug.js", 8, undefined)
     ]
     }
     }
     **/
    ];
};

/**
 * Add a cross-browser compatible registration service for
 * event handling/triggering.
 * @method createEventHandler
 * @private
 */
var createEventHandler = function() {
    RP.stash.addEventHandler = function() {
        if (window.addEventListener) {
            return function(el, eventName, fn, capture) {
                el.addEventListener(eventName, fn, capture);
            };

        }
        else if (window.attachEvent) {
            return function(el, eventName, fn, capture) {
                el.attachEvent("on" + eventName, fn);
            };

        }
        else {
            return function() {
            };

        }
    }();

};

/**
 * Determine where the stash root is so that additional libraries
 * are loaded according to the specified location..
 * @method determineStashRoot
 * @private
 * @return Stash location.
 */
var determineStashRoot = function() {
    if (typeof document !== "undefined") {
        var scriptTags = document.getElementsByTagName("head")[0].getElementsByTagName("script");

        for (var i = 0; i < scriptTags.length; i++) {
            if (scriptTags[i].src && scriptTags[i].src.match(/stashloader\.js$/i)) {
                return scriptTags[i].src.replace(/stashloader\.js$/i, "");
            }
        }
    }
    return "";
};

/**
 * Initiate the creation of the stash library API.
 * @method createStashAPI
 * @private
 */
var createStashAPI = function() {
    /**
     * The API used to provide functionality of the Stash Loader.
     * @singleton
     */
    RP.stash.api = function() {

        // Constants
        var CLIENT_MODE_MIN = "min";
        var CLIENT_MODE_DEBUG = "debug";
        var CLIENT_MODE_SRC = "src";

        var STASH_CLIENT_MODE_MIN = "min";
        var STASH_CLIENT_MODE_DEBUG = "debug";
        var STASH_CLIENT_MODE_SRC = "src";

        var MAX_BUILD = "MAX";
        var BUILD_DIGIT_PAD_LENGTH = 3;
        var TIP_BUILD = -2;

        var INCLUDE_TYPES = {
            CSS: "c",
            INLINE_JAVASCRIPT: "j",
            POST_LOAD_INLINE_JAVASCRIPT: "p",
            EXTERNAL_JAVASCRIPT: "s"
        }

        /**
         * Parses a library version to get major.minor version and build number.
         * If a version number does not contain major.minor.build it is considered invalid
         * and throws an error.
         * @method parseVersion
         * @param {String} version The version string.
         * @return {Object} Returns an object containing version and build.
         */
        var parseVersion = function(version) {
            var vtoks = version.split('.');
            if (vtoks.length !== 3 && vtoks.length !== 4) {
                throw new Error("Invalid version string: " + version);
            }
            var build = vtoks[vtoks.length - 1]; //vtoks.length === 3 ? vtoks[2] : vtoks[3];
            return {
                version: vtoks.slice(0, 2).join("."),
                drop: vtoks.length === 3 ? null : vtoks[2],
                build: build
            };
        };

        /**
         * Formats a number with a specified length prefixed by "0"'s.
         * @method formatNumber
         * @param {Number} num The number to prefix.
         * @param {Number} ndigits The desired length.
         * @return {String} The padded number.
         */
        var formatNumber = function(num, ndigits) {
            var str = num.toString();

            while (str.length < ndigits) {
                str = "0" + str;
            }
            return str;
        };

        /**
         * Generate the HTML necessary to load an external JS file.
         * @method includeScript
         * @private
         * @param {String} uri The url to the JS file.
         * @return {String} Generated HTML.
         */
        var includeScript = function(uri) {
            return '<script type="text/javascript" src="' + uri + '"></script>';
        };

        /**
         * Generate the HTML necessary to load an external stylesheet.
         * @method includeCss
         * @private
         * @param {String} uri The url to the stylesheet.
         * @return {String} Generated HTML.
         */
        var includeCss = function(uri) {
            return '<link rel="stylesheet" type="text/css" href="' + uri + '"/>';
        };

        /**
         * Copy the library definition to a new object.
         * @method copyLibraryDefinition
         * @param {Object} lib The library definition.
         * @return {Object} The newly cloned library definition.
         */
        var copyLibraryDefinition = function(lib) {
            // Copy all properties except the name (which is now a dictionary key)
            var copy = {};

            for (var prop in lib) {
                if (prop !== "n") {
                    copy[prop] = lib[prop];
                }
            }
            return copy;
        };
        
        /**
         * @class RP.stash.Resource
         * Represents a stash library resource.  Also provides an API around accessing the properties.
         */
        RP.stash.Resource = function(config) {
            var me = this;
            for (var prop in config) {
                me[prop] = config[prop];
            }
            
            // backwards compatability
            me.t = me.type;
            me.m = me.minified;
            me.u = me.url;
            me.j = me.inlineCode;
        };
        
        RP.stash.Resource.prototype = {
            getType: function() {
                return this.type;
            },
            
            isType: function(type) {
                return this.type === type;
            },
            
            isMinified: function() {
                return this.minified;
            },
            
            getUrl: function() {
                return this.url;
            },
            
            getInlineCode: function() {
                return this.inlineCode;
            },
            
            getDropMin: function() {
                return this.dmin;
            },
            
            getDropMax: function() {
                return this.dmax;
            },
            
            getBuildMin: function() {
                return this.bmin;
            },
            
            getBuildMax: function() {
                return this.bmax;
            }
        };

        /**
         * Defines a resource file specification.
         * @method defineResource
         * @param {String} type The resource type (j = JavaScript, c = CSS, etc...).
         * @param {Boolean} minified Flag indicating the library file as minified.
         * @param {String} url The path to the library file.
         * @param {Number} buildMin The minimum build version number for the specified library file.
         * @param {Number} buildMax The maximum build version number for the specified library file.
         * return {Object} The resource configuration object.
         */
        var defineResource = function(type, minified, url, dropMin, dropMax, buildMin, buildMax) {
            // backwards compatability
            if (buildMin == undefined && buildMax == undefined) {
                buildMin = dropMin;
                buildMax = dropMax;
                dropMin = undefined;
                dropMax = undefined;
            }

            return new RP.stash.Resource({
                type: type,
                minified: minified,
                url: url,
                dmin: dropMin,
                dmax: dropMax,
                bmin: buildMin,
                bmax: buildMax
            });
        };

        /**
         * Defines a resource file specification.  A substitution of "{STASHROOT}" will be
         * performed to properly reference the Stash root location.
         * @method
         * @param {String} type The resource type (j = JavaScript, c = CSS, etc...).
         * @param {Boolean} minified Flag indicating the library file as minified.
         * @param {String} inlineCode The inline block of JavaScript code.
         * @param {Number} buildMin The minimum build version number for the specified library file.
         * @param {Number} buildMax The maximum build version number for the specified library file.
         * @return {Object} The inline resource configuration object.
         */
        var defineInlineResource = function(type, minified, inlineCode, dropMin, dropMax, buildMin, buildMax) {
            inlineCode = inlineCode.replace(/\{STASHROOT\}/g, RP.stash.STASH_ROOT);

            // backwards compatability
            if (buildMin == undefined && buildMax == undefined) {
                buildMin = dropMin;
                buildMax = dropMax;
                dropMin = undefined;
                dropMax = undefined;
            }

            return new RP.stash.Resource({
                type: type,
                minified: minified,
                inlineCode: inlineCode,
                dmin: dropMin,
                dmax: dropMax,
                bmin: buildMin,
                bmax: buildMax
            });
        };

        /**
         * Replace any placeholders "{BUILD}" with the actual build number.
         *
         * @method replaceWithBuildNumber
         * @param {String} url The specific url with a possible replacement.
         * @param {String} buildNumber The build number.
         * @return {String} URL with the build number replacements.
         */
        var replaceWithBuildNumber = function(url, buildNumber) {
            if (url && (url.indexOf("{BUILD}") >= 0)) {
                url = url.replace(/\{BUILD\}/g, buildNumber);
            }
            return url;
        };

        /**
         * Replace any placeholders "{DROP}" with the actual drop number.
         *
         * @method replaceWithDropNumber
         * @param {String} url The specific url with a possible replacement.
         * @param {String} dropNumber The drop number.
         * @return {String} URL with the drop number replacements.
         */
        var replaceWithDropNumber = function(url, dropNumber) {
            if (url && (url.indexOf("{DROP}") >= 0)) {
                url = url.replace(/\{DROP\}/g, dropNumber);
            }
            return url;
        };

        /**
         * @method
         * Determine the mode based on the global RP.globals.STASH_CLIENTMODE.
         * @return {String} The client mode, CLIENT_MODE_MIN, CLIENT_MODE_DEBUG, CLIENT_MODE_SRC.
         */
        var getStashClientMode = function () {
            var mode = CLIENT_MODE_MIN;
            if (RP.globals && RP.globals.STASH_CLIENTMODE) {
                mode = RP.globals.STASH_CLIENTMODE;
            }
            return mode;
        };

        /**
         * @method
         * isMinified will determine if the mode passed in is "minified".
         * @param {String} mode The client mode.
         */
        var isMinified = function(mode) {
            return mode === CLIENT_MODE_MIN;
        }
        
        /**
         * Determines the drop number for the specified library, given a rawDrop.
         * If raw drop is undefined, then undefined is returned.
         */
        var determineDropNumber = function(library, rawDrop) {
            var dropNumber;
            if (rawDrop) {
                if (rawDrop === MAX_BUILD) {
                    dropNumber = parseInt(library.dmax);
                }
                else if (rawDrop === "*") {
                    throw new Error("The * drop is not a valid drop value.");
                }
                else {
                    dropNumber = parseInt(rawDrop, 10);
                }
            }
            return dropNumber;
        }
        
        /**
         * Determines the build string for the specified library, given a rawBuild and dropNumber.
         */
        var determineBuildString = function(library, rawBuild, dropNumber) {
            var buildString;
            if (rawBuild === "*") {
                buildString = "";
            }
            else {
                var tempBuild;
                
                if (rawBuild === MAX_BUILD) {
                    if (library.drops) {
                        tempBuild = library.drops[dropNumber].bmax;
                    }
                    else {
                        tempBuild = library.bmax
                    }
                }
                else {
                    tempBuild = rawBuild;
                }
                buildString = formatNumber(tempBuild, BUILD_DIGIT_PAD_LENGTH);
            }
            return buildString;
        };

        /**
         * Library name index to maintain a single definition and fast mapping
         * to the library definitions.
         * @private
         */
        var libraryDictionary = {};
        
        /** 
         * Used to track which libraries are loaded on the page.
         */
        var stashLibraryVersions = {};

        /**
         * List of succesfully loaded libraries.
         * @private
         */
        var librariesLoaded = [];

        /**
         * Index of the current library.
         * @private
         */
        var currentLibIdx = -1;

        return {
            /**
             * Add a library to the stash.
             * @method add
             * @param {Array} libs The library definitions.
             */
            add: function(libs) {
                for (var i = 0; i < libs.length; i++) {
                    var lib = libs[i];
                    libraryDictionary[lib.name] = copyLibraryDefinition(lib);
                }
            },

            /**
             * Retrieve the library includes for the library by version and minified configuration.
             * @method getLibraryIncludes
             * @param {String} name The name of the library.
             * @param {String} versionStr The version number to load.
             */
            getLibraryIncludes: function(name, versionStr) {
                var versionObj;

                try {
                    versionObj = parseVersion(versionStr);
                }
                catch (error) {
                    throw new Error("Version '" + versionStr + "' could not be parsed correctly.");
                }
                
                // Check for bad library.
                if (!libraryDictionary[name]) {
                    throw new Error("Can't find library '" + name + "' in the library dictionary.");
                }
                
                var dropNumber, buildNumber, buildString;
                var rawVersion = versionObj.version;
                var rawBuild = versionObj.build;
                var rawDrop = versionObj.drop;

                var finalIncludes = [];
                var library = libraryDictionary[name][rawVersion];
                var stashMode = getStashClientMode();
                
                if (library === undefined) {
                    throw new Error("Invalid version number '" + rawVersion + "'.");
                }

                // Figure out build and drop numbers
                dropNumber = determineDropNumber(library, rawDrop);
                buildString = determineBuildString(library, rawBuild, dropNumber);
                buildNumber = buildString === "" ?  TIP_BUILD : parseInt(buildString, 10);

                // Check for bad build number on library
                var includeList = library.files;
                var bmin, bmax;
                
                // Check for bad drop number on library
                if(rawDrop) {
                    if (dropNumber < library.dmin || dropNumber > library.dmax) {
                        throw new Error("Stash library '" + name + "' drop number '" + dropNumber + "' is not in the valid range of " + library.dmin + "-" + library.dmax);
                    }
                }
                
                // check if the library has drops, otherwise fallback to the legacy way
                if(library.drops) {
                    bmin = library.drops[dropNumber].bmin;
                    bmax = library.drops[dropNumber].bmax;
                }
                else {
                    bmin = library.bmin;
                    bmax = library.bmax;
                }
                
                if (buildNumber !== TIP_BUILD && (buildNumber < bmin || buildNumber > bmax)) {
                    throw new Error("Stash library '" + name + "' build number '" + buildNumber + "' is not in the valid range of " + bmin + "-" + bmax);
                }

                var setCurrentLibScript = "RP.stash.api.setCurrentLib({"
                + "name:'" + name + "',"
                + "version:'" + rawVersion + "',"
                + "build:'" + buildString + "',"
                + "minified:'" + stashMode + "'"
                + "});";
                finalIncludes.push(new RP.stash.Resource({
                    type: INCLUDE_TYPES.INLINE_JAVASCRIPT,
                    inlineCode: setCurrentLibScript
                }));

                for (var i = 0; i < includeList.length; i++) {
                    var include = includeList[i];

                    // match the minified flag to the resource's configuration of the minified setting.
                    if (typeof include.minified === "undefined" || include.minified === isMinified(stashMode)) {

                        // Skip include if there IS a build number and it's out of range.
                        if (buildNumber !== TIP_BUILD && (buildNumber < include.bmin || buildNumber > include.bmax)) {
                            continue;
                        }

                        // Skip include if the drop number is out of the drop range.
                        if (dropNumber !== undefined && (dropNumber < include.dmin || dropNumber > include.dmax)) {
                            continue;
                        }

                        // If requesting the tip, don't include old versions.
                        if (buildNumber === TIP_BUILD && include.bmax !== undefined) {
                            continue;
                        }

                        if (include.isType(INCLUDE_TYPES.INLINE_JAVASCRIPT)) {
                            finalIncludes.push(new RP.stash.Resource({
                                type: include.getType(),
                                inlineCode: replaceWithDropNumber(include.getInlineCode(), dropNumber)
                            }));
                        }
                        else if (include.isType(INCLUDE_TYPES.POST_LOAD_INLINE_JAVASCRIPT)) {
                            var script = "RP.stash.addEventHandler(window, \"load\", function() { " + include.getInlineCode() + " });";
                            finalIncludes.push(new RP.stash.Resource({
                                type: INCLUDE_TYPES.INLINE_JAVASCRIPT,
                                inlineCode: script
                            }));
                        }
                        else {
                            // Do {BUILD} substitution.
                            if (stashMode === STASH_CLIENT_MODE_SRC) {
                                buildString = "";
                            }
                            var url = replaceWithBuildNumber(include.getUrl(), (buildString === "" ? "" : "-") + buildString);
                            url = replaceWithDropNumber(url, dropNumber);

                            // Do {client-mode} substitution.
                            url = url.replace(/\{client-mode\}/, stashMode);

                            finalIncludes.push(new RP.stash.Resource({
                                type: include.getType(),
                                url: url
                            }));
                        }
                    }
                }

                return finalIncludes;
            },

            /**
             * Generate the HTML to load the libraries for the library based on the version and minified flag.
             * @method getLibraryIncludeHTML
             * @param {String} name The name of the library.
             * @param {String} versionStr The version number to load.
             */
            getLibraryIncludeHTML: function(name, versionStr) {
                var includes;
                var html = [];
                
                try {
                    includes = this.getLibraryIncludes(name, versionStr);
                }
                catch(error) {
                    window.alert(error.message);
                    html.push("<!-- ERROR: " + error.message + " -->");
                    return html;
                }
               
                if (includes.length === 0) {
                    html.push("<!-- WARNING: Library found, but no version/minified match found: name=" + name + "; version=" + versionStr + " -->");
                    return html;
                }

                for (var i = 0; i < includes.length; i++) {
                    var include = includes[i];

                    if (include.isType(INCLUDE_TYPES.EXTERNAL_JAVASCRIPT)) {
                        html.push(includeScript(include.getUrl()));
                    }
                    else if (include.isType(INCLUDE_TYPES.CSS)) {
                        html.push(includeCss(include.getUrl()));
                    }
                    else if (include.isType(INCLUDE_TYPES.INLINE_JAVASCRIPT)) {  // inline javascript
                        html.push('<script type="text/javascript">\r\n  ' + include.getInlineCode() + '\r\n</' + 'script>');
                    }
                    else {
                        html.push("<!-- Internal error: unhandled include type: " + include.getType() + " -->");
                    }
                }

                return html;
            },

            /**
             * Load a library into the context of the current running application.
             * @method loadLibrary
             * @param {String} name The name of the library.
             * @param {String} versionStr The version number to load.
             */
            loadLibrary: function(name, version) {
                if ((typeof Ext !== "undefined") && Ext.isReady) {
                    // Cannot load library since document is already rendered.
                    alert("Error: Cannot call loadLibrary() after document has rendered.  Use ScriptLoader.loadStashLibrary() instead.");
                    return;
                }
                else {
                    var html = this.getLibraryIncludeHTML(name, version);
                    try {
                        stashLibraryVersions[name] = parseVersion(version);
                    }
                     catch(error) {
                        window.alert(error.message);
                        html.push("<!-- ERROR: " + error.message + " -->");
                        return html;
                    }
                    document.write(html.join("\r\n"));
                }
            },
            
            getVersion: function(name) {
                var versionObj = stashLibraryVersions[name];
                if(versionObj === undefined) {
                    throw new Error(versionObj = "The version " + name + " is not a valid library");
                }
                
                var library = libraryDictionary[name][versionObj.version];
                if(library === undefined) {
                    throw new Error("The library " + name + " is not a valid library");
                }
                
                var retObj = {};
                retObj.version = versionObj.version;
                retObj.drop = determineDropNumber(library, versionObj.drop);
                retObj.build = determineBuildString(library, versionObj.build, retObj.drop);
                return retObj;
            },

            /**
             * List the available libraries and their version/configuration information.
             * @method listLibraries
             */
            listLibraries: function() {
                var list = [];
                var addLibraryObject = function(version, bmin, bmax) {
                    list.push({
                        name: library.name,
                        version: version,
                        minBuild: bmin,
                        maxBuild: bmax
                    });
                };

                for (var key in libraryDictionary) {
                    var library = libraryDictionary[key];

                    for (var version in library) {
                        if (version.match(/\d+\.\d+/)) {
                            // if there is a drop min/max loop through and build the list with the versions.
                            if(library[version].dmin !== undefined && library[version].dmax !== undefined) {
                                for(var j = library[version].dmin; j <= library[version].dmax; j++) {
                                    var drop = library[version].drops[j];
                                    if (drop) {
                                        addLibraryObject(version + "." + j, drop.bmin, drop.bmax);
                                    }
                                }
                            }
                            else {
                                addLibraryObject(version, library[version].bmin, library[version].bmax);
                            }
                        }
                    }
                }
                return list;
            },

            /**
             * List the library specifics for a given version.
             * @method listLibraryComponents
             * @param {String} libName Library name.
             * @param {String} version Version number.
             */
            listLibraryComponents: function(libName, version) {
                var lib = libraryDictionary[libName];
                return lib[version];
            },

            /**
             * Signal the beginning of a library load.
             * @method beginLoadLib
             */
            beginLoadLib: function() {
                currentLibIdx++;
            },

            /**
             * Signal the end of the library load.
             * @method endLoadLib
             */
            endLoadLib: function() {
                currentLibIdx--;
                librariesLoaded.pop();
            },

            /**
             * Retrieve the library information for the library currently
             * being loaded into the application context.
             * @method getCurrentLib
             */
            getCurrentLib: function() {
                return librariesLoaded[currentLibIdx];
            },

            /**
             * Set the current library loading into the application context.
             * @method setCurrentLib
             * @param {String} lib The library name being loaded.
             */
            setCurrentLib: function(lib) {
                librariesLoaded.push(lib);
            },

            //============================================================
            // Helper functions
            //============================================================
            /**
             * Defines a CSS library file specification.
             * @method defineCss
             * @param {Boolean} minified Flag indicating the library file as minified.
             * @param {String} uri The path to the library file.
             * @param {Number} dropMin The minimum drop version number for the specified library file.
             * @param {Number} dropMax The maximum drop version number for the specified library file.
             * @param {Number} buildMin The minimum build version number for the specified library file.
             * @param {Number} buildMax The maximum build version number for the specified library file.
             * @return {String} The CSS resource definition.
             */
            defineCss: function(minified, uri, dropMin, dropMax,  buildMin, buildMax) {
                return defineResource(INCLUDE_TYPES.CSS, minified, uri, dropMin, dropMax, buildMin, buildMax);
            },

            /**
             * Shorthand for {@link RP.stash.api#defineCss}
             * @method defineExternalCss
             * @param {Boolean} minified Flag indicating the library file as minified.
             * @param {String} uri The path to the library file.
             * @param {Number} dropMin The minimum drop version number for the specified library file.
             * @param {Number} dropMax The maximum drop version number for the specified library file.
             * @param {Number} buildMin The minimum build version number for the specified library file.
             * @param {Number} buildMax The maximum build version number for the specified library file.
             * @return {String} The CSS resource definition.
             * @deprecated
             */
            defineExternalCss: function() {
                return RP.stash.api.defineCss.apply(RP.stash.api, arguments);
            },

            /**
             * Utilizes {@link RP.stash.api#defineCss} to load a CSS resource relative to the Stash location.
             * @method defineStashCss
             * @param {Boolean} minified Flag indicating the library file as minified.
             * @param {String} uri The path to the library file.
             * @param {Number} dropMin The minimum drop version number for the specified library file.
             * @param {Number} dropMax The maximum drop version number for the specified library file.
             * @param {Number} buildMin The minimum build version number for the specified library file.
             * @param {Number} buildMax The maximum build version number for the specified library file.
             * @return {String} The CSS resource definition relative to the Stash.
             */
            defineStashCss: function(minified, uri, dropMin, dropMax, buildMin, buildMax) {
                return RP.stash.api.defineCss(minified, RP.stash.STASH_ROOT + uri, dropMin, dropMax, buildMin, buildMax);
            },

            /**
             * Defines a JS library file specification.
             * @method defineScript
             * @param {Boolean} minified Flag indicating the library file as minified.
             * @param {String} uri The path to the library file.
             * @param {Number} dropMin The minimum drop version number for the specified library file.
             * @param {Number} dropMax The maximum drop version number for the specified library file.
             * @param {Number} buildMin The minimum build version number for the specified library file.
             * @param {Number} buildMax The maximum build version number for the specified library file.
             * @return {String} The JS resource definition.
             */
            defineScript: function(minified, uri, dropMin, dropMax, buildMin, buildMax) {
                return defineResource(INCLUDE_TYPES.EXTERNAL_JAVASCRIPT, minified, uri, dropMin, dropMax, buildMin, buildMax);
            },

            /**
             * Shorthand for {@link RP.stash.api#defineScript}
             * @method defineLib
             * @param {Boolean} minified Flag indicating the library file as minified.
             * @param {String} uri The path to the library file.
             * @param {Number} dropMin The minimum drop version number for the specified library file.
             * @param {Number} dropMax The maximum drop version number for the specified library file.
             * @param {Number} buildMin The minimum build version number for the specified library file.
             * @param {Number} buildMax The maximum build version number for the specified library file.
             * @return {String} The JS resource definition.
             * @deprecated
             */
            defineLib: function() {
                return RP.stash.api.defineScript.apply(RP.stash.api, arguments);
            },

            /**
             * Shorthand {@link RP.stash.api#defineLib} to load an local JS library.
             * @method defineStashScript
             * @param {Boolean} minified Flag indicating the library file as minified.
             * @param {String} uri The path to the library file.
             * @param {Number} dropMin The minimum drop version number for the specified library file.
             * @param {Number} dropMax The maximum drop version number for the specified library file.
             * @param {Number} buildMin The minimum build version number for the specified library file.
             * @param {Number} buildMax The maximum build version number for the specified library file.
             * @return {String} The JS resource definition relative to the Stash.
             */
            defineStashScript: function(minified, uri, dropMin, dropMax, buildMin, buildMax) {
                return RP.stash.api.defineScript(minified, RP.stash.STASH_ROOT + uri, dropMin, dropMax, buildMin, buildMax);
            },

            /**
             * Shorthand {@link RP.stash.api#defineStashScript} to load an local JS library.
             * @method defineLocalLib
             * @param {Boolean} minified Flag indicating the library file as minified.
             * @param {String} uri The path to the library file.
             * @param {Number} dropMin The minimum drop version number for the specified library file.
             * @param {Number} dropMax The maximum drop version number for the specified library file.
             * @param {Number} buildMin The minimum build version number for the specified library file.
             * @param {Number} buildMax The maximum build version number for the specified library file.
             * @return {String} The JS resource definition relative to the Stash.
             * @deprecated
             */
            defineLocalLib: function() {
                return RP.stash.api.defineStashScript.apply(RP.stash.api, arguments);
            },

            /**
             * Defines an inline JavaScript block to be loaded.
             * @method defineInlineScript
             * @param {Boolean} minified Flag indicating the library file as minified.
             * @param {String} js The JavaScript block.
             * @param {Number} dropMin The minimum drop version number for the specified library file.
             * @param {Number} dropMax The maximum drop version number for the specified library file.
             * @param {Number} buildMin The minimum build version number for the specified library file.
             * @param {Number} buildMax The maximum build version number for the specified library file.
             * @return {String} The inline JS resource definition.
             */
            defineInlineScript: function(minified, js, dropMin, dropMax, buildMin, buildMax) {
                return defineInlineResource(INCLUDE_TYPES.INLINE_JAVASCRIPT, minified, js, dropMin, dropMax, buildMin, buildMax);
            },

            /**
             * Defines a post-inline JavaScript block to be loaded.
             * @method definePostLoadInlineScript
             * @param {Boolean} minified Flag indicating the library file as minified.
             * @param {String} js The JavaScript block.
             * @param {Number} dropMin The minimum drop version number for the specified library file.
             * @param {Number} dropMax The maximum drop version number for the specified library file.
             * @param {Number} buildMin The minimum build version number for the specified library file.
             * @param {Number} buildMax The maximum build version number for the specified library file.
             * @return {String} The post-inline JS resource definition.
             */
            definePostLoadInlineScript: function(minified, js, dropMin, dropMax, buildMin, buildMax) {
                return defineInlineResource(INCLUDE_TYPES.POST_LOAD_INLINE_JAVASCRIPT, minified, js, dropMin, dropMax, buildMin, buildMax);
            }
        };
    }();
};

(function() {
    if (typeof RP === "undefined") {
        RP = {};
    }
    
    RP.stash = {};

    // add a method for handling event registration to handle
    // special event firing where required.
    createEventHandler();

    // Find the location this script loaded from.
    RP.stash.STASH_ROOT = determineStashRoot();

    // set the stash API
    createStashAPI();

    // load the library definitions
    RP.stash.api.add(getLibraryDefinitions());
})();

/**
 * Shortcut to stash API version of loadLibrary.
 * @method loadLibrary
 */
RP.stash.loadLibrary = RP.stash.api.loadLibrary;
/**
 * Shortcut to stash API version of getLibraryIncludes.
 * @method getLibraryIncludes
 */
RP.stash.getLibraryIncludes = RP.stash.api.getLibraryIncludes;
/**
 * Shortcut to stash API version of getLibraryIncludeHTML.
 * @method getLibraryIncludeHTML
 */
RP.stash.getLibraryIncludeHTML = RP.stash.api.getLibraryIncludeHTML;
/**
 * Shortcut to stash API version of listLibraries.
 * @method listLibraries
 */
RP.stash.listLibraries = RP.stash.api.listLibraries;

/**
 * Shortcut to stash API version of getVersion.
 * @method getVersion
 */
RP.stash.getVersion = RP.stash.api.getVersion;

// backwards compat.
window.redprairie = window.RP;
