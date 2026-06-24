/**
 * Minimal client session for static mall pages (no JWT yet).
 * - save(member): persist after login
 * - load(): { member, loggedInAt } or null
 * - clear(): logout
 */
(function (global) {
    var KEY = 'gulimall_session';

    function save(member) {
        var payload = {
            member: member || null,
            loggedInAt: Date.now()
        };
        try {
            global.localStorage.setItem(KEY, JSON.stringify(payload));
        } catch (e) {
            /* private mode / quota */
        }
    }

    function clear() {
        try {
            global.localStorage.removeItem(KEY);
        } catch (e) {
            /* ignore */
        }
    }

    function load() {
        try {
            var raw = global.localStorage.getItem(KEY);
            if (!raw) {
                return null;
            }
            var trimmed = String(raw).replace(/^\uFEFF/, '').trim();
            return JSON.parse(trimmed);
        } catch (e) {
            return null;
        }
    }

    /** Treat stored member as logged-in only when it has a usable identifier (matches login-page checks). */
    function resolveMember(payload) {
        var m = payload && payload.member;
        if (m == null || typeof m !== 'object') {
            return null;
        }
        if (Array.isArray(m)) {
            return null;
        }
        if (m.id != null && m.id !== '') {
            return m;
        }
        if (typeof m.username === 'string' && m.username.trim()) {
            return m;
        }
        if (typeof m.nickname === 'string' && m.nickname.trim()) {
            return m;
        }
        return Object.keys(m).length ? m : null;
    }

    /** Align with {@code src/lib/gulimallSession.ts} oauthMemberSessionFetchUrl. */
    function oauthMemberSessionFetchUrl() {
        if (typeof global.GULIMALL_AUTH_SESSION_URL === 'string' && global.GULIMALL_AUTH_SESSION_URL) {
            return global.GULIMALL_AUTH_SESSION_URL;
        }
        var loc = global.location || {};
        var h = loc.hostname;
        if (h === 'localhost' || h === '127.0.0.1') {
            return loc.protocol + '//' + h + ':88/api/auth/oauth/member/session';
        }
        /* Explicit origin so behaviour matches same-host API behind nginx (/api/ → gateway). */
        var origin = loc.origin || '';
        return origin ? origin + '/api/auth/oauth/member/session' : '/api/auth/oauth/member/session';
    }

    function displayName(member) {
        if (!member) {
            return '';
        }
        var n = member.nickname;
        if (typeof n === 'string' && n.trim()) {
            return n.trim();
        }
        var u = member.username;
        if (typeof u === 'string' && u.trim()) {
            return u.trim();
        }
        return 'User';
    }

    function syncFromServer(done) {
        var url = oauthMemberSessionFetchUrl();
        if (!global.fetch) {
            if (done) {
                done();
            }
            return;
        }
        global
            .fetch(url, {
                credentials: 'include',
                mode: 'cors',
                cache: 'no-store'
            })
            .then(function (r) {
                if (!r.ok) {
                    throw new Error('session fetch http ' + r.status);
                }
                var ct = r.headers && r.headers.get ? r.headers.get('content-type') : '';
                if (ct && ct.indexOf('json') === -1) {
                    throw new Error('session fetch not json');
                }
                return r.json();
            })
            .then(function (data) {
                var code = data && data.code;
                var ok = code === undefined || code === 0;
                if (ok && data && data.member) {
                    save(data.member);
                }
                if (done) {
                    done();
                }
            })
            .catch(function () {
                if (done) {
                    done();
                }
            });
    }

    /**
     * Updates header sign-in / register line for static pages (search, item detail).
     * opts: signInId, registerLiId, guestSignInText (when logged out after paint).
     */
    function mountTopBar(opts) {
        opts = opts || {};
        var signInId = opts.signInId || 'gulimall-search-signin';
        var registerLiId = opts.registerLiId || 'gulimall-search-register-li';
        var guestText = opts.guestSignInText != null ? opts.guestSignInText : 'Sign in';

        function paint() {
            var payload = load();
            var m = resolveMember(payload);
            var signIn = document.getElementById(signInId);
            var regLi = document.getElementById(registerLiId);
            if (m) {
                if (signIn) {
                    signIn.textContent = 'Hi, ' + displayName(m);
                    signIn.setAttribute('href', '/');
                    signIn.setAttribute('title', 'Mall home');
                    /* Remove guest “Sign in” emphasis (e.g. search-page .li_2 { color: red }) */
                    if (signIn.classList) {
                        signIn.classList.remove('li_2');
                    }
                }
                if (regLi) {
                    regLi.style.display = 'none';
                }
            } else {
                if (signIn) {
                    signIn.textContent = guestText;
                    signIn.setAttribute('href', '/login');
                    signIn.setAttribute('title', 'Sign in');
                    if (signIn.classList) {
                        signIn.classList.add('li_2');
                    }
                }
                if (regLi) {
                    regLi.style.display = '';
                }
            }
        }

        var payload = load();
        if (resolveMember(payload)) {
            paint();
        }
        syncFromServer(paint);
        function deferredRepaint() {
            [0, 50, 200].forEach(function (ms) {
                global.setTimeout(function () {
                    paint();
                }, ms);
            });
        }
        deferredRepaint();
        if (global.addEventListener) {
            global.addEventListener('load', function onLoadSession() {
                global.removeEventListener('load', onLoadSession);
                syncFromServer(paint);
                deferredRepaint();
            });
        }
    }

    global.GulimallSession = {
        STORAGE_KEY: KEY,
        save: save,
        clear: clear,
        load: load,
        resolveMember: resolveMember,
        oauthMemberSessionFetchUrl: oauthMemberSessionFetchUrl,
        displayName: displayName,
        syncFromServer: syncFromServer,
        mountTopBar: mountTopBar
    };
})(typeof window !== 'undefined' ? window : this);
