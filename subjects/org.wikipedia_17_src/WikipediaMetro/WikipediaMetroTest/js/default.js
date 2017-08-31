﻿// For an introduction to the Blank template, see the following documentation:
// http://go.microsoft.com/fwlink/?LinkId=232509
(function () {
    "use strict";
    var app = WinJS.Application;

    // Set up data bindings for the search results list - empty initial
    WinJS.Namespace.define("SearchResults", {
        itemList: new WinJS.Binding.List([])
    });
    WinJS.Namespace.define("HubContents", {
        itemList: new WinJS.Binding.List([])
    });
    WinJS.Namespace.define("TocSections", {
        itemList: new WinJS.Binding.List([])
    });
    TocSections.itemList.dataSource.itemFromDescription = function (desc) {
        console.log('itemFromDescription hack: ' + JSON.stringify(desc));
        return WinJS.Promise.wrap({
            index: 0
        });
    };

    // State stack!
    var state = {
        stack: [],
        current: function () {
            return state.stack[state.stack.length - 1];
        },
        push: function (newState) {
            state.stack.push(newState);
        },
        pop: function () {
            return state.stack.pop();
        }
    };

    (function () {
        // Create the groups for the ListView from the item data and the grouping functions
        HubContents.groupedList = HubContents.itemList.createGrouped(getGroupKey, getGroupData, compareGroups);

        // Function used to sort the groups
        // Note: This is similar to default sorting behavior 
        //   when using WinJS.Binding.List.createGrouped()
        function compareGroups(left, right) {
            var sort = ['Featured pictures', 'Featured articles', 'On this day', 'Recent changes', 'Spacer'];
            var n = sort.indexOf(left),
                n2 = sort.indexOf(right);
            return n - n2;
        }

        // Function which returns the group key that an item belongs to
        function getGroupKey(dataItem) {
            return dataItem.group;
        }

        // Function which returns the data for a group
        function getGroupData(dataItem) {
            return {
                title: dataItem.groupText
            };
        }
    })();

    function md5(str) {
        var crypto = Windows.Security.Cryptography,
            buffer = crypto.CryptographicBuffer.convertStringToBinary(str, crypto.BinaryStringEncoding.Utf8),
            provider = crypto.Core.HashAlgorithmProvider.openAlgorithm('MD5'),
            buffHash = provider.hashData(buffer),
            strHash = crypto.CryptographicBuffer.encodeToHexString(buffHash);
        return strHash;
    }

    function tileId(lang, title) {
        return 'Wikipedia.' + lang + '.' + md5(title.replace(/_/g, ' '));
    }

    window.preferencesDB = {
        get: function (key) {
            return 'en';
        }
    }
    window.ROOT_URL = '';

    $(function () {
        $(document).bind('mw-messages-ready', function () {
            WinJS.UI.processAll().then(doStuff);
        });
        l10n.initLanguages();

        function doStuff() {
            $('#readInCmd')[0].winControl.label = mediaWiki.message('menu-language').plain();
            $('#pinCmd')[0].winControl.label = mediaWiki.message('menu-win8-pin').plain();
            $('#unpinCmd')[0].winControl.label = mediaWiki.message('menu-win8-unpin').plain();
            $('#browserCmd')[0].winControl.label = mediaWiki.message('menu-open-browser').plain();
            $('#offline').localize();

            WinJS.Application.onsettings = function (e) {
                e.detail.applicationcommands = {
                    about: {
                        title: mediaWiki.message('menu-about').plain(),
                        href: "/about.html"
                    }
                };
                WinJS.UI.SettingsFlyout.populateSettings(e);
            }

            initHub('en');
            // Handler for links!
            $(document).on('click', 'a', function (event) {
                var url = $(this).attr('href'),
                    refMatches = url.match(/^#cite_note/),
                    hashMatches = url.match(/^#/),
                    wikiMatches = url.match(/^\/wiki\/(.*)/);
                if (refMatches) {
                    // Reference link
                    var $ref = $(url).clone();
                    showLightbox($ref, 'small');
                    event.preventDefault();
                } else if (hashMatches) {
                        // no-op, but close any lightboxes first
                    $('.lightbox-bg, .lightbox-fg').remove();
                } else if (wikiMatches) {
                        // Internal wiki-link
                    $('.lightbox-bg, .lightbox-fg').remove();
                    var lang = state.current().lang,
                        title = decodeURIComponent(wikiMatches[1]);
                    if ($(this).hasClass('image')) {
                        // Image link
                        showImage(lang, title);
                    } else {
                        doLoadPage(lang, title);
                    }
                    event.preventDefault();
                } else {
                    // Remote or absolute link
                    if (url.match(/^\/\//)) {
                        // fixup for protocol-relative links
                        url = 'https:' + url;
                    }
                    var uri = new Windows.Foundation.Uri(url);
                    Windows.System.Launcher.launchUriAsync(uri);
                    event.preventDefault();
                }
            });
            $('#back').click(function () {
                //doShowHub();
                doGoBack();
            }).bind('contextmenu', function (event) {
                // Catch touch context menu
                event.preventDefault();
                showHistoryMenu(this);
            });
            $('#resultlist').bind('iteminvoked', function (event) {
                var index = event.originalEvent.detail.itemIndex;
                var selected = SearchResults.itemList.getItem(index);
                console.log(selected);
                if (!selected.data.title) {
                    throw new Error("bad title");
                }
                doLoadPage(state.current().lang, selected.data.title);
            });
            $('#hub-list').bind('iteminvoked', function (event) {
                var index = event.originalEvent.detail.itemIndex;
                var selected = HubContents.groupedList.getItem(index);
                if (selected.data.title) {
                    doLoadPage(state.current().lang, selected.data.title);
                }
            });
            $(window).bind('resize', function () {
                sizeContent();
            });
            $(window).resize();

            $('#appbar').bind('beforeshow', function () {
                var lang = state.current().lang,
                    title = state.current().title;
                if (Windows.UI.StartScreen.SecondaryTile.exists(tileId(lang, title))) {
                    $("#pinCmd").hide();
                    $("#unpinCmd").show();
                } else {
                    $("#unpinCmd").hide();
                    $("#pinCmd").show();
                }
                if (state.current().type == 'article') {
                    $('#pinCmd').removeAttr('disabled');
                    $('#unpinCmd').removeAttr('disabled');
                } else {
                    $('#pinCmd').attr('disabled', 'disabled');
                    $('#unpinCmd').attr('disabled', 'disabled');
                }
            });

            $('#pinCmd').click(function () {
                var lang = state.current().lang,
                    title = state.current().title.replace(/_/g, ' '),
                    shortName = title,
                    displayName = title + ' - Wikipedia',
                    tileOptions = Windows.UI.StartScreen.TileOptions.showNameOnLogo,
                    uriLogo = new Windows.Foundation.Uri("ms-appx:///images/secondary-tile.png"),
                    tileActivationArguments = "lang=" + lang + '&' + 'title=' + encodeURIComponent(title),
                    tile = new Windows.UI.StartScreen.SecondaryTile(tileId(lang, title), shortName, displayName, tileActivationArguments, tileOptions, uriLogo);

                var element = document.getElementById("pinCmd"),
                    selectionRect = element.getBoundingClientRect();
                tile.requestCreateAsync({ x: selectionRect.left, y: selectionRect.top }).then(function (isCreated) {
                    if (isCreated) {
                        console.log('tile is created');
                        // Secondary tile successfully pinned.
                    } else {
                        console.log('tile is not created');
                        // Secondary tile not pinned.
                    }
                });
            });

            $('#unpinCmd').click(function () {
                var lang = state.current().lang,
                    title = state.current().title.replace(/_/g, ' '),
                    tile = Windows.UI.StartScreen.SecondaryTile(tileId(lang, title)),
                    element = document.getElementById("unpinCmd"),
                    selectionRect = element.getBoundingClientRect();
                tile.requestDeleteAsync({ x: selectionRect.left, y: selectionRect.top });
            });

            $('#browserCmd').click(function () {
                var url = articleUrl(state.current().lang, state.current().title),
                    uri = new Windows.Foundation.Uri(url);
                Windows.System.Launcher.launchUriAsync(uri);
            });

            $('#readInCmd').click(function () {
                var current = state.current(),
                    title = current.title,
                    lang = current.lang,
                    promise;
                if (current.type == 'article') {
                    promise = getLanguageLinks(lang, title);
                } else {
                    promise = getWikiLanguageLinks(lang);
                }
                promise.then(function (langlinks) {
                    var div = document.createElement('div');
                    langlinks.forEach(function (langlink) {
                        var lang = langlink.lang,
                            target = langlink.target,
                            label = langlink.title + ' (' + lang + ')',
                            button = document.createElement('button'),
                            command = new WinJS.UI.MenuCommand(button, {
                                label: label
                            });
                        command.addEventListener('click', function () {
                            if (current.type == 'hub') {
                                initHub(lang);
                            } else if (current.type == 'search') {
                                doSearch(lang, current.search);
                            } else {
                                doLoadPage(lang, target);
                            }
                        });
                        div.appendChild(button);
                    });
                    $('body').append(div);
                    var menu = new WinJS.UI.Menu(div, {
                        anchor: $('#readInCmd')[0]
                    });
                    menu.show();
                });
            });

            app.start();
        };
    });

    function getLanguageLinks(lang, title) {
        var url = 'https://' + lang + '.wikipedia.org/w/api.php';
        return new WinJS.Promise(function (complete, error, progress) {
            $.ajax({
                url: url,
                data: {
                    action: 'query',
                    prop: 'langlinks',
                    titles: title,
                    lllimit: 250,
                    format: 'json'
                },
                success: function (data) {
                    var langlinks = [];
                    if (data.query && data.query.pages) {
                        $.each(data.query.pages, function (i, page) {
                            page.langlinks.forEach(function (link) {
                                langlinks.push({
                                    lang: link.lang,
                                    target: link['*'],
                                    title: link['*']
                                });
                            });
                        });
                    }
                    complete(langlinks);
                },
                error: function (msg) {
                    error(msg)
                }
            });
        });
    }

    function getWikiLanguageLinks(lang) {
        var url = 'https://' + lang + '.wikipedia.org/w/api.php';
        return new WinJS.Promise(function (complete, error, progress) {
            $.ajax({
                url: url,
                data: {
                    action: 'sitematrix',
                    format: 'json'
                },
                success: function (data) {
                    var langlinks = [];
                    if (data.sitematrix) {
                        var matrix = data.sitematrix,
                            langlinks = [];
                        $.each(matrix, function(index, lang) {
                            if (index.match(/^\d+$/)) {
                                lang.site.forEach(function(site) {
                                    if (site.code == 'wiki') {
                                        langlinks.push({
                                            lang: lang.code,
                                            target: '',
                                            title: lang.name
                                        });
                                    }
                                });
                            }
                        });
                    }
                    complete(langlinks);
                },
                error: function (msg) {
                    error(msg)
                }
            });
        });
    }

    function parseArgs(query) {
        var args = {},
            parts = query.split('&');
        parts.forEach(function (chunk) {
            var bits = chunk.split('='),
                name = decodeURIComponent(bits[0]),
                val = decodeURIComponent(bits[1]);
            args[name] = val;
        });
        return args;
    }

    app.onactivated = function (eventObject) {
        var detail = eventObject.detail;
        if (detail.kind === Windows.ApplicationModel.Activation.ActivationKind.launch) {
            if (detail.previousExecutionState !== Windows.ApplicationModel.Activation.ApplicationExecutionState.terminated) {
                // TODO: This application has been newly launched. Initialize C:\Users\brion\src\wiki\WikipediaMetroTest\WikipediaMetroTest\js\default.js
                // your application here.
            } else {
                // TODO: This application has been reactivated from suspension. 
                // Restore application state here.
            }
            if (detail.arguments != '') {
                console.log(detail.arguments);
                var args = parseArgs(detail.arguments);
                doLoadPage(args.lang, args.title);
            }
        } else if (detail.kind === Windows.ApplicationModel.Activation.ActivationKind.search) {
            doSearch(state.current().lang, detail.queryText);
        }
    };
    
    app.oncheckpoint = function (eventObject) {
        // TODO: This application is about to be suspended. Save any state
        // that needs to persist across suspensions here. You might use the 
        // WinJS.Application.sessionState object, which is automatically
        // saved and restored across suspension. If you need to complete an
        // asynchronous operation before your application is suspended, call
        // eventObject.setPromise(). 
    };

    // Obtain the Search Pane object and register for handling search while running as the main application
    var searchPane = Windows.ApplicationModel.Search.SearchPane.getForCurrentView();
    searchPane.addEventListener("querysubmitted", function (e) {
        console.log('querysubmitted', e);
        doLoadPage(state.current().lang, e.queryText);
    });
    var request;
    // Register to Handle Suggestion Request
    searchPane.addEventListener("suggestionsrequested", function (e) {
        console.log('suggestionsrequested', e);
        var suggestionRequest = e.request,
            queryText = e.queryText;
        // Indicate that we'll obtain suggestions asynchronously:
        var deferral = suggestionRequest.getDeferral();

        // This refers to a local package file that contains a sample JSON response. You can update the Uri to a service that supports this standard in order to see suggestions come from a web service.  In order for the updated Uri to work it must also be included in the ApplicationContentUriRules in the manifest
        var suggestionUri = "https://" + state.current().lang + ".wikipedia.org/w/api.php?action=opensearch&namespace=0&suggest=&search=";
        // If you are using a webservice,the query string should be encoded into the URI. See example below:
        suggestionUri += encodeURIComponent(queryText);

        // Cancel the previous suggestion request if it is not finished
        if (request && request.abort) {
            request.abort();
        }

        // Create request to obtain suggestions from service and supply them to the Search Pane
        $.ajax({
            url: suggestionUri,
            success: function(data, textstatus, request) {
                if (data && data instanceof Array) {
                    var suggestions = data[1];
                    if (suggestions) {
                        suggestionRequest.searchSuggestionCollection.appendQuerySuggestions(suggestions);
                        console.log("Suggestions provided for query: " + queryText);
                    } else {
                        console.log("No suggestions provided for query: " + queryText);
                    }
                }
                deferral.complete(); // Indicate we're done supplying suggestions.
            }
        });
    });
    // Handle the selection of a Result Suggestion for Scenario 6
    searchPane.addEventListener("resultsuggestionchosen", function (e) {
        console.log('search', e);
        doLoadPage(state.current().lang, e.queryText);
    });

    function stripHtmlTags(html) {
        if (typeof html !== 'string') {
            throw new Error('must be string');
        }
        //return html.replace(/<[^>]+>/g, ''); // fixme put in real html parser
        return $('<div>').html(html).text();
    }

    function doSearch(lang, query) {
        state.push({
            type: 'search',
            lang: lang,
            title: '',
            search: query
        });
        $('#offline').hide();
        $('#hub').hide();
        $('#back').show();
        $('#reader').hide();
        $('#search').show();
        $('#title').text(query);
        var url = 'https://' + lang + '.wikipedia.org/w/api.php';
        $('#spinner').show();
        $.ajax({
            url: url,
            data: {
                action: 'query',
                list: 'search',
                srwhat: 'text',
                srsearch: query,
                srlimit: 21,
                format: 'json'
            },
            success: function (data) {
                $('#spinner').hide();
                // data.query.search
                // [
                //   {ns, size, snippet, timestamp, title, wordcount
                // ]
                // data.query.searchinfo
                //   totalhits
                if (data.error) {
                    // ..
                    $("#content").text('Search error');
                } else {
                    // Replace the current list
                    var list = SearchResults.itemList;
                    list.splice(0, list.length);

                    data.query.search.forEach(function (item, i) {
                        list.push({
                            title: item.title,
                            snippet: stripHtmlTags(item.snippet)
                        });
                    });
                }
            },
            error: function (xhr, status, err) {
                $('#spinner').hide();
                $('#offline').show();
                state.current().error = true;
            }
        });
    }

    function clearSearch() {
        // Clear the results list
        var list = SearchResults.itemList;
        list.splice(0, list.length);
        $('#search').hide();
        $('#title').text('Wikipedia');
    }

    function doLoadPage(lang, title) {
        state.push({
            type: 'article',
            lang: lang,
            title: title
        });
        $('#offline').hide();
        $('#hub').hide();
        $('#back').show();
        clearSearch();
        $('#content').empty();
        $('#title').text(title.replace(/_/g, ' '));
        $('#reader').show();
        sizeContent();

        $('#spinner').show();
        $.ajax({
            url: 'https://' + lang + '.wikipedia.org/w/api.php',
            data: {
                action: 'mobileview',
                page: title,
                sections: 'all',
                format: 'json'
            },
            success: function (data) {
                $('#spinner').hide();
                if (data.error) {
                    // No exact match? Go do a search.
                    state.pop(); // skip this one in history
                    doSearch(lang, title);
                    return;
                }
                /*
                mobileview
                    .normlizedtitle
                    sections [
                        {
                            id
                            text
                        }
                        {
                            toclevel
                            line
                            id
                        }
                    ]

                */
                $('#content').empty();
                TocSections.itemList.splice(0, TocSections.itemList.length); // clear
                data.mobileview.sections.forEach(function (section) {
                    if (!section.text) {
                        return;
                    }
                    var div = insertWikiHtml('#content', section.text);
                    if (section.id == 0) {
                        TocSections.itemList.push({
                            title: title.replace(/_/g, ' '),
                            style: 'tocitem-0',
                            element: div
                        });
                    } else {
                        TocSections.itemList.push({
                            key: TocSections.itemList.length,
                            title: section.line,
                            style: 'tocitem-' + section.toclevel,
                            element: div
                        });
                    }
                });
                $('#content').append('<div class="column-spacer"></div>');
            },
            error: function (xhr, status, err) {
                $('#spinner').hide();
                $('#offline').show();
                state.current().error = true;
            }
        });
    }

    // Outgoing sharing
    var dataTransferManager = Windows.ApplicationModel.DataTransfer.DataTransferManager.getForCurrentView();
    dataTransferManager.addEventListener("datarequested", function (e) {
        var request = e.request,
            title = state.current().title,
            lang = state.current().lang,
            url = articleUrl(state.current().lang, title);
        request.data.setUri(new Windows.Foundation.Uri(url));
        request.data.properties.title = title + ' - Wikipedia';
        request.data.properties.description = 'Link to Wikipedia article';
    });

    function articleUrl(lang, title) {
        if (typeof title != 'string') {
            throw new Error('bad title input to articleUrl');
        }
        if (typeof lang != 'string') {
            throw new Error('bad lang input to articleUrl');
        }
        return 'https://' + lang + '.wikipedia.org/wiki/' + encodeURIComponent(title.replace(/ /g, '_'));
    }


    // Live tile stuff
    function updateLiveTile(title, content) {
        var Notifications = Windows.UI.Notifications;
        var tileXml = Notifications.TileUpdateManager.getTemplateContent(Notifications.TileTemplateType.tileWideText09);
        var tileAttributes = tileXml.getElementsByTagName("text");
        tileAttributes[0].appendChild(tileXml.createTextNode(title));
        tileAttributes[1].appendChild(tileXml.createTextNode(content));
        var tileNotification = new Notifications.TileNotification(tileXml);
        var currentTime = new Date();
        tileNotification.expirationTime = new Date(currentTime.getTime() + 600 * 1000);
        Notifications.TileUpdateManager.createTileUpdaterForApplication().update(tileNotification);
    }
    function fetchFeed(lang, feed, callback) {
        $.ajax({
            url: "https://" + lang + ".wikipedia.org/w/api.php",
            data: {
                action: 'featuredfeed',
                feed: feed,
                feedformat: 'atom'
            },
            success: function (data, textstatus, request) {
                if (request.responseXML) {
                    var summaries = request.responseXML.getElementsByTagName('summary');
                    var htmlList = [];
                    for (var i = summaries.length - 1; i >= 0; i--) {
                        var summary = summaries[i];
                        htmlList.push(summary.textContent);
                    }
                    callback(htmlList);
                } else {
                    callback([]);
                }
            },
            error: function (xhr, status, err) {
                callback([], status);
            }
        });
    }

    function insertWikiHtml(target, html) {
        // hack for protocol-relative images (unsafe)
        if (typeof html !== "string") {
            throw new Error('we got a non-string');
        }
        html = html.replace(/"\/\/upload\.wikimedia\.org/g, '"https://upload.wikimedia.org');
        var $div = $('<div>');
        MSApp.execUnsafeLocalFunction(function () {
            $div.append(html);
        });
        /*
        $div.find('img').each(function () {
            // hack for protocol-relative images
            var $img = $(this),
                src = $img.attr('src');
            if (src.substr(0, 2) == '//') {
                $img.attr('src', 'https:' + src);
            }
        });
        */
        $div.find('table').each(function () {
            var $table = $(this);
            var $embedded = $table.parent().closest('table');
            if ($embedded.length > 0) {
                // Embedded in another table
                return;
            }
            var passes = [
                //'infobox',
                //'metadata',
                //'ombox',
                'cquote'
            ];
            for (var i = 0; i < passes.length; i++) {
                if ($table.hasClass(passes[i])) {
                    // Usually we want these inline: infoboxes fit a single column and have important data,
                    // and metadata bits are usually small.
                    return;
                }
            }
            var txt;
            if ($table.hasClass('infobox')) {
                txt = mediaWiki.message('table-show-infobox').plain();
            } else if ($table.hasClass('metadata')) {
                txt = mediaWiki.message('table-show-meta').plain();
            } else {
                txt = mediaWiki.message('table-show').plain();
            }
            var $placeholder = $('<button>')
                .addClass('show-table')
                .append($('<div>').text(txt))
                .click(function () {
                    console.log('yeah');
                    showLightbox($table);
                })
                .insertAfter($table);
            $table.detach();
            var $img = $table.find('img:eq(0)');
            if ($img.length) {
                // If there's a neat image, copy it in!
                $img.clone().appendTo($placeholder);
            }
            // Extracted float-right tables look nicer centered
            $table.addClass('table-view');
        });
        $(target).append($div);
        return $div[0];
    }

    function doShowHub(lang) {
        state.push({
            type: 'hub',
            lang: lang,
            title: ''
        });
        $('#title').text(mediaWiki.message('sitename').plain());
        $('#search').hide();
        $('#reader').hide();
        $('#back').hide();
        $('#hub').show();
        $('#offline').hide();
        sizeContent();
    }

    function getMainPage(lang) {
        return new WinJS.Promise(function (complete, error, progress) {
            var url = 'https://' + lang + '.wikipedia.org/w/api.php';
            $.ajax({
                url: url,
                data: {
                    action: 'query',
                    meta: 'allmessages',
                    ammessages: 'mainpage',
                    amenableparser: 1,
                    format: 'json'
                },
                success: function (data, status, xhr) {
                    var title = 'Main Page';
                    data.query.allmessages.forEach(function (msg) {
                        if (msg.name == 'mainpage') {
                            title = msg['*'];
                        }
                    });
                    complete(title);
                },
                error: function (xhr, err) {
                    error(err);
                }
            });
        });
    }

    function getRecentChanges(lang) {
        return new WinJS.Promise(function (complete, error, progress) {
            var url = 'https://' + lang + '.wikipedia.org/w/api.php';
            $.ajax({
                url: url,
                data: {
                    action: 'query',
                    list: 'recentchanges',
                    rctoponly: 1,
                    format: 'json'
                },
                success: function (data) {
                    complete(data.query.recentchanges);
                },
                error: function (xhr, status, err) {
                    complete([], err);
                }
            });
        });
    }

    function initHub(lang) {
        doShowHub(lang);

        // Empty any old contents
        var list = HubContents.itemList;
        list.splice(0, list.length);

        var pings = 4, nErrors = 0, nItems = 0;
        var completeAnother = function () {
            pings--;
            if (pings == 0) {
                $('#spinner').hide();
                list.push({
                    title: '',
                    heading: '',
                    snippet: '',
                    image: '#',
                    group: 'Spacer',
                    groupText: ' ',
                    style: 'spacer-item'
                });
                console.log('errors: ' + nErrors);
                if (nErrors) {
                    $('#offline').show();
                }
            }
        };

        $('#spinner').show();
        fetchFeed(lang, 'featured', function (htmlList, err) {
            if (err) {
                nErrors++;
            }
            var html;
            if (htmlList.length) {
                var txt = stripHtmlTags(htmlList[0]);
                updateLiveTile(mediaWiki.message('win8-tile-featured-article').plain(), txt);
            }
            htmlList.slice(0, 8).forEach(function (html, index) {
                var $html = $('<div>').html(html),
                    $links = $html.find('a'),
                    $imgs = $html.find('img'),
                    title = '',
                    image = '';
                for (var i = 0; i < $links.length; i++) {
                    var $link = $($links[i]);
                    if ($link.find('img').length) {
                        // Skip a link containing an image
                        continue;
                    }
                    title = extractWikiTitle($link.attr('href'));
                    break;
                }
                if ($imgs.length) {
                    image = $imgs.attr('src');
                    if (image.substr(0, 2) == '//') {
                        image = 'https:' + image;
                    }
                } else {
                    image = '/images/secondary-tile.png';
                }
                nItems++;
                list.push({
                    title: title,
                    heading: '',
                    snippet: stripHtmlTags(html).substr(0, 100) + '...',
                    image: image,
                    group: 'Featured articles',
                    groupText: mediaWiki.message('section-featured-articles').plain(),
                    style: (index < 1) ? 'featured-item large' : 'featured-item'
                });
            });
            completeAnother();
        });
        fetchFeed(lang, 'potd', function (htmlList, err) {
            if (err) {
                nErrors++;
            }
            $('#spinner').hide();
            htmlList.slice(0, 6).forEach(function (html, index) {
                var $html = $('<div>').html(html),
                    $links = $html.find('a'),
                    $imgs = $html.find('img'),
                    title = '',
                    image = '';
                for (var i = 0; i < $links.length; i++) {
                    var $link = $($links[i]);
                    if ($link.find('img').length) {
                        // Skip a link containing an image
                        continue;
                    }
                    try {
                        title = extractWikiTitle($link.attr('href'));
                        break;
                    } catch (e) {
                        // Not an internal link?
                        continue;
                    }
                }
                if ($imgs.length) {
                    image = $imgs.attr('src');
                    if (image.substr(0, 2) == '//') {
                        image = 'https:' + image;
                    }
                }
                var imageid = ("img" + Math.random()).replace('.', '');
                nItems++;
                list.push({
                    title: title,
                    heading: '',
                    snippet: '',
                    image: image,
                    imageid: imageid,
                    group: 'Featured pictures',
                    groupText: mediaWiki.message('section-featured-pictures').plain(),
                    style: (index == 0) ? 'photo-item large' : 'photo-item'
                });

                //fetchImage(state.current().lang, image, 600, 600, function (img) {
                //    $('#' + imageid).attr('src', img);
                //});
            });
            completeAnother();
        });
        fetchFeed(lang, 'onthisday', function (htmlList, err) {
            if (err) {
                nErrors++;
            }
            if (htmlList.length) {
                var html = htmlList[0],
                    $html = $('<div>').html(html),
                    $lis = $html.find('li');
                $lis.each(function () {
                    var $li = $(this),
                        txt = stripHtmlTags($li.html()),
                        $link = $li.find('b a'),
                        title = extractWikiTitle($link.attr('href') + '');
                    var bits = txt.split(' – '),
                        year = bits[0],
                        detail = bits[1];
                    nItems++;
                    list.push({
                        title: title,
                        heading: year,
                        snippet: detail,
                        image: '',
                        group: 'On this day',
                        groupText: mediaWiki.message('section-onthisday').plain(),
                        style: 'onthisday-item'
                    });
                });
            }
            completeAnother();
        });
        getRecentChanges(lang).then(function (recentchanges, err) {
            if (err) {
                nErrors++;
            }
            recentchanges.forEach(function (change) {
                if (change.ns == 0 && change.type == 'edit') {
                    nItems++;
                    list.push({
                        title: change.title,
                        heading: '',
                        snippet: '',
                        image: '/images/secondary-tile.png',
                        group: 'Recent changes',
                        groupText: mediaWiki.message('section-recentchanges').plain(),
                        style: 'featured-item'
                    });
                }
            });
            completeAnother();
        });
    }

    function sizeContent() {
        var $work, fudge;

        // Hack to swap orientation in snapped mode
        if (window.innerWidth <= 320) {
            // Snapped
            $('#toc')[0].winControl.layout = new WinJS.UI.ListLayout();
            $('#hub-list')[0].winControl.layout = new WinJS.UI.ListLayout({
                groupInfo: groupInfo
            });
            $('#content').scrollLeft(0); // avoid being scrolled off into nothingness
        } else {
            // Not snapped
            $('#toc')[0].winControl.layout = new WinJS.UI.GridLayout();
            $('#hub-list')[0].winControl.layout = new WinJS.UI.GridLayout({
                groupInfo: groupInfo
            });
        }

        if ($('#hub').is(':visible')) {
            $work = $('#hub-list');
            fudge = 0;
        } else {
            //$work = $('#content, #toc');
            $work = $('#semanticZoomer');
            fudge = 60;
        }
        var top = 150;
        //var top = $work.position().top; // sometimes wrong during switch???
        var h = $(window).height() - top - fudge;
        //$('#semanticZoomer').css('height', $(window).height() - top);
        $work.css('height', h + 'px');
    }

    function showLightbox(element, className) {
        var $bg = $('<div>').addClass('lightbox-bg').appendTo('body'),
            $fg = $('<div>').addClass('lightbox-fg').appendTo('body');
        if (className) {
            $fg.addClass(className);
        }
        $fg.append(element);
        var close = function () {
            $(element).detach();
            $fg.remove();
            $bg.remove();
        };
        $fg.click(close);
        $bg.click(close);
        $(document).bind('keypress.lightbox', function (event) {
            if (event.keyCode == 27) {
                // ESC
                $bg.click();
                $(document).unbind('keypress.lightbox');
            }
        });
    }

    function extractWikiTitle(url) {
        var wikiMatches = url.match(/\/wiki\/(.*)/);
        if (wikiMatches) {
            return decodeURIComponent(wikiMatches[1]).replace(/_/g, ' ');
        } else {
            throw new Error('not a wiki url');
        }
    }

    function showImage(lang, title) {
        $.ajax({
            url: 'https://' + lang + '.wikipedia.org/w/api.php',
            data: {
                action: 'query',
                titles: title,
                prop: 'imageinfo',
                iiprop: 'url',
                iiurlwidth: 600,
                iiurlheight: 600,
                format: 'json'
            },
            success: function (data) {
                var imageinfo;
                $.each(data.query.pages, function(i, item) {
                    imageinfo = item.imageinfo[0];
                });
                console.log(JSON.stringify(imageinfo));
                var $img = $('<img>')
                    .attr('src', imageinfo.thumburl)
                    .addClass('imageview');
                showLightbox($img);
            }
        });
    }

    function doGoBack() {
        var discard = state.pop(),
            redo = state.pop();
        if (redo.type == 'hub') {
            doShowHub(redo.lang);
        } else if (redo.type == 'search') {
            doSearch(redo.lang, redo.search);
        } else {
            doLoadPage(redo.lang, redo.title);
        }

    }

    function showHistoryMenu(anchor) {
        var div = document.createElement('div');
        state.stack.slice().reverse().forEach(function (item, n) {
            if (n == 0) {
                return;
            }
            var lang = item.lang,
                target = item.title,
                label;
            if (item.type == 'hub') {
                label = mediaWiki.message('sitename').plain() + ' (' + item.lang + ')';
            } else if (item.type == 'search') {
                label = item.search + ' (' + item.lang + ')';
            } else {
                label = item.title + ' (' + item.lang + ')';
            }
            var button = document.createElement('button'),
                command = new WinJS.UI.MenuCommand(button, {
                    label: label
                });
            command.addEventListener('click', function () {
                state.stack.splice(state.stack.length - n - 1, n + 1);
                if (item.type == 'hub') {
                    initHub(lang);
                } else if (item.type == 'search') {
                    doSearch(lang, item.search);
                } else {
                    doLoadPage(lang, target);
                }
            });
            div.appendChild(button);
        });
        $('body').append(div);
        var menu = new WinJS.UI.Menu(div, {
            anchor: anchor
        });
        menu.show();
    }
})();
function groupInfo() {
    return {
        enableCellSpanning: true,
        cellWidth: 50,
        cellHeight: 80
    };
}
