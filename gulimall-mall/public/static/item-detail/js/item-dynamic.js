(function () {
    function q(selector) {
        return document.querySelector(selector);
    }

    function markLoading() {
        var fadeNodes = ['.box-name', '.box-hide', '.box-summary', '.box-attr-3', '#li2 .guiGebox', '#itemDetailGallery'];
        for (var i = 0; i < fadeNodes.length; i++) {
            var n = q(fadeNodes[i]);
            if (n) {
                n.classList.add('item-dynamic-fade', 'item-dynamic-loading');
            }
        }
        var title = q('.box-name');
        if (title) title.innerHTML = '<span class="item-skeleton-line" style="width:460px;"></span>';
        var sub = q('.box-hide');
        if (sub) sub.innerHTML = '<span class="item-skeleton-line" style="width:320px;"></span>';
        var price = q('.box-summary ul li:nth-child(2) span:last-child');
        if (price) {
            price.classList.add('item-summary-price');
            price.innerHTML = '<span class="item-skeleton-line" style="width:90px;height:24px;"></span>';
        }
        var attrs = q('.box-attr-3');
        if (attrs) attrs.innerHTML = '<div class="item-skeleton-block" style="height:110px;"></div>';
        var groups = q('#li2 .guiGebox');
        if (groups) groups.innerHTML = '<div class="item-skeleton-block" style="height:210px;"></div>';
        var detailGallery = q('#itemDetailGallery');
        if (detailGallery) detailGallery.classList.add('item-dynamic-loading');
        var imgbox = q('.imgbox');
        if (imgbox) imgbox.classList.add('item-dynamic-loading');
        var thumbBox = q('.box-lh-one');
        if (thumbBox) thumbBox.classList.add('item-dynamic-loading');
    }

    function unmarkLoading() {
        var fadeNodes = ['.box-name', '.box-hide', '.box-summary', '.box-attr-3', '#li2 .guiGebox', '#itemDetailGallery'];
        for (var i = 0; i < fadeNodes.length; i++) {
            var n = q(fadeNodes[i]);
            if (n) n.classList.remove('item-dynamic-loading');
        }
        var detailGallery = q('#itemDetailGallery');
        if (detailGallery) detailGallery.classList.remove('item-dynamic-loading');
        var imgbox = q('.imgbox');
        if (imgbox) imgbox.classList.remove('item-dynamic-loading');
        var thumbBox = q('.box-lh-one');
        if (thumbBox) thumbBox.classList.remove('item-dynamic-loading');
    }

    function getSkuId() {
        var p = new URLSearchParams(window.location.search || '');
        var fromQuery = p.get('skuId') || '';
        if (fromQuery) return fromQuery;
        var m = (window.location.pathname || '').match(/\/(\d+)\.html$/);
        return m ? m[1] : '';
    }

    function safeText(v) {
        if (v == null) return '';
        return String(v).trim();
    }

    function escapeHtml(s) {
        return String(s)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function parseSkuIdsAttr(raw) {
        var s = safeText(raw);
        if (!s) return [];
        return s.split(',').map(function (x) { return parseInt(x.trim(), 10); }).filter(function (n) { return !isNaN(n); });
    }

    function navigateIfSingleSkuFromSelection() {
        var box = q('.box-attr-3');
        if (!box) return;
        var rows = box.querySelectorAll('.box-attr-2 dl');
        if (!rows.length) return;
        var lists = [];
        for (var r = 0; r < rows.length; r++) {
            var sel = rows[r].querySelector('dd.redborder');
            if (!sel) return;
            lists.push(parseSkuIdsAttr(sel.getAttribute('data-sku-ids')));
        }
        var result = lists[0];
        for (var i = 1; i < lists.length; i++) {
            var next = lists[i];
            result = result.filter(function (id) { return next.indexOf(id) >= 0; });
        }
        if (result.length !== 1) return;
        var skuId = String(result[0]);
        if (skuId !== String(getSkuId())) {
            location.href = '/' + skuId + '.html';
        }
    }

    function bindSaleAttrSkuNav(currentSkuId) {
        var box = q('.box-attr-3');
        if (!box) return;
        var cur = parseInt(String(currentSkuId || ''), 10);
        var rows = box.querySelectorAll('.box-attr-2 dl');
        for (var r = 0; r < rows.length; r++) {
            var dds = rows[r].querySelectorAll('dd');
            var picked = null;
            if (!isNaN(cur)) {
                for (var d = 0; d < dds.length; d++) {
                    var ids = parseSkuIdsAttr(dds[d].getAttribute('data-sku-ids'));
                    if (ids.indexOf(cur) >= 0) {
                        picked = dds[d];
                        break;
                    }
                }
            }
            if (!picked && dds.length) picked = dds[0];
            for (var j = 0; j < dds.length; j++) dds[j].classList.remove('redborder');
            if (picked) picked.classList.add('redborder');
        }
        if (box._saleAttrNavBound) return;
        box._saleAttrNavBound = true;
        box.addEventListener('click', function (e) {
            var a = e.target && e.target.closest && e.target.closest('a');
            if (!a) return;
            var dd = a.closest('dd');
            if (!dd || !dd.closest('.box-attr-2')) return;
            e.preventDefault();
            var row = dd.closest('dl');
            if (row) {
                var sibs = row.querySelectorAll('dd');
                for (var k = 0; k < sibs.length; k++) sibs[k].classList.remove('redborder');
            }
            dd.classList.add('redborder');
            navigateIfSingleSkuFromSelection();
        });
    }

    function setText(selector, value) {
        var el = document.querySelector(selector);
        if (el) el.textContent = value;
    }

    function setPrice(price) {
        var target = document.querySelector('.box-summary ul li:nth-child(2) span:last-child');
        if (!target) return;
        var n = Number(price);
        target.classList.add('item-summary-price');
        target.textContent = Number.isFinite(n) ? n.toFixed(2) : '--';
    }

    function imageUrlsFromRows(rows) {
        var list = [];
        var seen = {};
        for (var i = 0; i < (rows || []).length; i++) {
            var u = safeText(rows[i] && rows[i].imgUrl);
            if (u && !seen[u]) {
                seen[u] = true;
                list.push(u);
            }
        }
        return list;
    }

    function parseIntroImageUrls(descText) {
        var text = safeText(descText);
        if (!text) return [];
        var parts = text.split(',').map(function (x) { return safeText(x); }).filter(function (x) { return !!x; });
        if (!parts.length) return [];
        var allHttp = true;
        for (var i = 0; i < parts.length; i++) {
            if (!/^https?:\/\//i.test(parts[i])) {
                allHttp = false;
                break;
            }
        }
        return allHttp ? parts : [];
    }

    /** SPU gallery (Product Images) + intro long images (Product Introduction / decript). */
    function buildProductGalleryUrls(item) {
        var urls = imageUrlsFromRows(item.spuImages);
        var intro = parseIntroImageUrls(item.desc && item.desc.decript);
        for (var i = 0; i < intro.length; i++) {
            if (urls.indexOf(intro[i]) === -1) urls.push(intro[i]);
        }
        return urls;
    }

    function ensureDetailGalleryContainer() {
        var li2 = q('#li2');
        var specs = q('#li2 .guiGebox');
        if (!li2 || !specs) return null;
        var box = q('#itemDetailGallery');
        if (box) return box;
        box = document.createElement('div');
        box.id = 'itemDetailGallery';
        box.className = 'item-spu-gallery item-detail-tab-gallery';
        box.hidden = true;
        box.innerHTML =
            '<p class="item-spu-gallery-title">Product gallery</p><ul class="item-spu-gallery-list"></ul>';
        specs.parentNode.insertBefore(box, specs.nextSibling);
        return box;
    }

    function paintGalleryList(ul, urls, clickable) {
        if (!ul) return;
        ul.innerHTML = urls
            .map(function (src) {
                return '<li><img src="' + escapeHtml(src) + '" alt="" loading="lazy"/></li>';
            })
            .join('');
        if (!clickable) return;
        ul.onclick = function (e) {
            var img = e.target && e.target.tagName === 'IMG' ? e.target : null;
            if (!img) return;
            var src = img.getAttribute('src');
            if (!src) return;
            var main1 = q('.probox img.img1');
            var main2 = q('.showbox img.img1');
            if (main1) main1.setAttribute('src', src);
            if (main2) main2.setAttribute('src', src);
        };
    }

    function renderProductGallery(urls) {
        var list = urls || [];
        var tabBox = ensureDetailGalleryContainer();
        if (tabBox) {
            if (!list.length) {
                tabBox.hidden = true;
                paintGalleryList(tabBox.querySelector('.item-spu-gallery-list'), [], false);
            } else {
                tabBox.hidden = false;
                paintGalleryList(tabBox.querySelector('.item-spu-gallery-list'), list, true);
            }
        }
        var leftBox = q('#itemSpuGallery');
        if (leftBox) leftBox.hidden = true;
    }

    /** Left thumbnails: SKU images only (SPU gallery is in the tab section below). */
    function setImages(skuRows, fallback) {
        var list = imageUrlsFromRows(skuRows);
        if (!list.length && fallback) list.push(fallback);
        if (!list.length) return;

        var main1 = document.querySelector('.probox img.img1');
        var main2 = document.querySelector('.showbox img.img1');
        if (main1) main1.setAttribute('src', list[0]);
        if (main2) main2.setAttribute('src', list[0]);

        var ul = q('.box-lh-one ul');
        if (!ul) return;
        ul.innerHTML = list
            .map(function (src) {
                return '<li><img src="' + src + '"/></li>';
            })
            .join('');
        bindThumbNav();
    }

    function bindThumbNav() {
        var ul = q('.box-lh-one ul');
        if (!ul) return;
        var main1 = q('.probox img.img1');
        var main2 = q('.showbox img.img1');

        function activateThumb(li) {
            if (!li) return;
            var img = li.querySelector('img');
            var src = img && img.getAttribute('src');
            if (!src) return;
            var lis = ul.querySelectorAll('li');
            for (var i = 0; i < lis.length; i++) {
                lis[i].classList.remove('item-thumb-active');
                lis[i].style.padding = '1px';
                lis[i].style.border = 'none';
            }
            li.classList.add('item-thumb-active');
            li.style.padding = '0';
            li.style.border = 'solid 1px red';
            if (main1) main1.setAttribute('src', src);
            if (main2) main2.setAttribute('src', src);
        }

        if (ul._thumbNavBound) return;
        ul._thumbNavBound = true;
        ul.addEventListener('click', function (e) {
            var li = e.target && e.target.closest && e.target.closest('li');
            if (!li || !ul.contains(li)) return;
            e.preventDefault();
            activateThumb(li);
        });
        ul.addEventListener('mouseover', function (e) {
            var li = e.target && e.target.closest && e.target.closest('li');
            if (!li || !ul.contains(li)) return;
            activateThumb(li);
        });

        var firstLi = ul.querySelector('li');
        if (firstLi) activateThumb(firstLi);
    }

    function normalizeSaleAttrValue(entry) {
        if (entry == null) return null;
        if (typeof entry === 'string') {
            var t = safeText(entry);
            return t ? { attrValue: t, skuIds: [] } : null;
        }
        var av = safeText(entry.attrValue);
        if (!av) return null;
        var ids = Array.isArray(entry.skuIds) ? entry.skuIds : [];
        var skuIds = [];
        for (var i = 0; i < ids.length; i++) {
            var n = Number(ids[i]);
            if (Number.isFinite(n)) skuIds.push(n);
        }
        return { attrValue: av, skuIds: skuIds };
    }

    function storageSortKey(value) {
        var tb = /(\d+)\s*TB/i.exec(value || '');
        if (tb) return parseInt(tb[1], 10) * 1024;
        var gb = /(\d+)\s*GB/i.exec(value || '');
        if (gb) return parseInt(gb[1], 10);
        return 999999;
    }

    function sortSaleAttrValues(attrName, list) {
        var n = (attrName || '').toLowerCase();
        if (n !== 'ram' && n !== 'memory' && n !== 'capacity' && n !== 'version') {
            return list;
        }
        return list.slice().sort(function (a, b) {
            var ka = storageSortKey(a.attrValue);
            var kb = storageSortKey(b.attrValue);
            if (ka !== kb) return ka - kb;
            return a.attrValue.localeCompare(b.attrValue);
        });
    }

    function renderSaleAttrs(saleAttr, currentSkuId) {
        var box = document.querySelector('.box-attr-3');
        if (!box) return;
        if (!saleAttr || !saleAttr.length) return;

        var html = '';
        for (var i = 0; i < saleAttr.length; i++) {
            var sa = saleAttr[i] || {};
            var values = Array.isArray(sa.attrValues) ? sa.attrValues : [];
            var name = safeText(sa.attrName) || ('属性' + (sa.attrId || ''));
            var normalized = [];
            for (var j = 0; j < values.length; j++) {
                var nv0 = normalizeSaleAttrValue(values[j]);
                if (nv0) normalized.push(nv0);
            }
            normalized = sortSaleAttrValues(name, normalized);
            var cls = 'box-attr-2 clear';
            html += '<div class="' + cls + '"><dl><dt>' + escapeHtml(name) + '</dt>';
            for (var k = 0; k < normalized.length; k++) {
                var nv = normalized[k];
                var idsStr = nv.skuIds.join(',');
                html +=
                    '<dd data-sku-ids="' +
                    escapeHtml(idsStr) +
                    '"><a href="#">' +
                    escapeHtml(nv.attrValue) +
                    '</a></dd>';
            }
            html += '</dl></div>';
        }
        box.setAttribute('data-dynamic-sale-attr', '1');
        box.innerHTML = html;
        bindSaleAttrSkuNav(currentSkuId);
    }

    function renderGroupAttrs(groupAttrs) {
        var box = document.querySelector('#li2 .guiGebox');
        if (!box) return;
        if (!groupAttrs || !groupAttrs.length) return;

        var html = '';
        for (var i = 0; i < groupAttrs.length; i++) {
            var g = groupAttrs[i] || {};
            var attrs = Array.isArray(g.attrs) ? g.attrs : [];
            var gName = safeText(g.groupName) || ('参数组' + (g.groupId || ''));
            html += '<div class="guiGe"><h3>' + gName + '</h3><dl>';
            for (var j = 0; j < attrs.length; j++) {
                var a = attrs[j] || {};
                var aName = safeText(a.attrName) || ('属性' + (a.attrId || ''));
                var aVal = safeText(a.attrValue) || '--';
                html += '<dt>' + aName + '</dt><dd>' + aVal + '</dd>';
            }
            html += '</dl></div>';
        }
        box.innerHTML = html;
    }


    function setStockText(hasStock) {
        var stockLi = q('.box-stock > .box-ul > li:nth-child(3)');
        if (!stockLi) return;
        if (hasStock) {
            stockLi.innerHTML = '<span class="item-stock-in">In stock</span>, shipped by warehouse';
        } else {
            stockLi.innerHTML = '<span class="item-stock-out">Out of stock</span>, temporarily unavailable';
        }
    }

    function getQty() {
        var inp = q('#itemQtyInput');
        if (!inp) return 1;
        var n = parseInt(String(inp.value || '1'), 10);
        if (isNaN(n) || n < 1) return 1;
        return n;
    }

    function buildCartAddUrl(skuId, qty, title, img) {
        var params = new URLSearchParams();
        params.set('skuId', String(skuId));
        params.set('num', String(Math.max(1, qty)));
        if (title) params.set('title', title);
        if (img) params.set('img', img);
        return '/api/cart/add?' + params.toString();
    }

    var cartAddState = { skuId: '', title: '', img: '' };
    var cartAddListenersBound = false;

    function refreshCartAddUrl() {
        var link = q('#itemAddToCartLink');
        var sticky = q('#itemAddToCartStickyBtn');
        if (!cartAddState.skuId) return;
        var url = buildCartAddUrl(cartAddState.skuId, getQty(), cartAddState.title, cartAddState.img);
        if (link) link.setAttribute('href', url);
        if (sticky) {
            sticky.onclick = function () {
                window.location.href = url;
            };
        }
    }

    /** Updates add-to-cart URLs; binds qty / +/- listeners only once (safe to call from render multiple times). */
    function setCartAddTarget(skuId, title, img) {
        cartAddState.skuId = String(skuId);
        cartAddState.title = title || '';
        cartAddState.img = img || '';
        refreshCartAddUrl();
        if (!cartAddListenersBound) {
            cartAddListenersBound = true;
            var qtyInput = q('#itemQtyInput');
            if (qtyInput) {
                qtyInput.addEventListener('change', refreshCartAddUrl);
                qtyInput.addEventListener('input', refreshCartAddUrl);
            }
            document.addEventListener(
                'click',
                function (e) {
                    var t = e.target;
                    if (t && (t.id === 'jia' || t.id === 'jian')) {
                        setTimeout(refreshCartAddUrl, 0);
                    }
                },
                true
            );
        }
    }

    function fetchStockStatus(skuId) {
        if (!skuId) return;
        fetch('/api/ware/waresku/hasStock', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify([Number(skuId)]),
        })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (!res || Number(res.code) !== 0 || !Array.isArray(res.data) || !res.data.length) return;
                var one = res.data[0] || {};
                setStockText(!!one.hasStock);
            })
            .catch(function () {
                // Keep existing stock text when stock API fails.
            });
    }

    function render(item) {
        var info = item.info || {};
        var title = safeText(info.skuTitle) || safeText(info.skuName) || ('SKU ' + (info.skuId || ''));
        var subtitle = safeText(info.skuSubtitle) || safeText(info.skuDesc) || '';

        if (title) {
            document.title = title;
            setText('.box-name', title);
        }
        if (subtitle) setText('.box-hide', subtitle);
        setPrice(info.price);
        setImages(item.images || [], safeText(info.skuDefaultImg));
        renderSaleAttrs(item.saleAttr || [], info.skuId != null ? info.skuId : getSkuId());
        renderGroupAttrs(item.groupAttrs || []);
        renderProductGallery(buildProductGalleryUrls(item));
        fetchStockStatus(info.skuId);
        var sid = info.skuId != null ? info.skuId : getSkuId();
        var imgUrl = safeText(info.skuDefaultImg);
        setCartAddTarget(sid, title, imgUrl);
    }

    function showError(skuId) {
        var msg = '商品不存在或已下架';
        if (skuId) msg += ' (skuId: ' + skuId + ')';
        setText('.box-name', msg);
        setText('.box-hide', '请返回搜索页选择其他商品。');
        unmarkLoading();
    }

    function init() {
        var skuId = getSkuId();
        markLoading();
        if (!skuId) {
            showError('');
            return;
        }
        setCartAddTarget(skuId, '', '');
        setTimeout(unmarkLoading, 5000);
        fetch('/api/product/item/' + encodeURIComponent(skuId), { credentials: 'include' })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (!res || Number(res.code) !== 0 || !res.item || !res.item.info) {
                    throw new Error('invalid item payload');
                }
                try {
                    render(res.item);
                } catch (e) {
                    showError(skuId);
                    return;
                }
                setTimeout(unmarkLoading, 180);
            })
            .catch(function () {
                showError(skuId);
            });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
