/**
 * Search page: fetches /api/search/product/list by keyword and filters,
 * renders product list and only the filters related to current results (brands, category, attributes).
 */
(function () {
    var breadcrumbContainerSelector = '.JD_ipone .JD_ipone_bar';
    var defaultBreadcrumbHtml = '';

    function getQueryParam(name) {
        var params = new URLSearchParams(location.search);
        return params.get(name) || '';
    }

    /** Build search URL with optional overrides (e.g. add/remove one brand or attr). */
    function buildSearchUrl(override) {
        var p = new URLSearchParams(location.search);
        var keyword = p.get('keyword') || '';
        if (override) {
            if (override.keyword !== undefined) keyword = override.keyword;
            if (override.pageNum !== undefined) p.set('pageNum', String(override.pageNum));
            if (override.catalog3Id !== undefined) {
                var currentCatalogs = p.getAll('catalog3Id').slice();
                var catalogId = String(override.catalog3Id);
                var catalogIdx = currentCatalogs.indexOf(catalogId);
                if (override.catalog3Id === null || override.catalog3Id === '') {
                    p.delete('catalog3Id');
                } else if (catalogIdx >= 0) {
                    currentCatalogs.splice(catalogIdx, 1);
                    p.delete('catalog3Id');
                    currentCatalogs.forEach(function (c) { p.append('catalog3Id', c); });
                } else {
                    currentCatalogs.push(catalogId);
                    p.delete('catalog3Id');
                    currentCatalogs.forEach(function (c) { p.append('catalog3Id', c); });
                }
            }
            if (override.brandId !== undefined) {
                var current = p.getAll('brandId').slice();
                var id = String(override.brandId);
                var idx = current.indexOf(id);
                if (idx >= 0) current.splice(idx, 1);
                else current.push(id);
                p.delete('brandId');
                current.forEach(function (b) { p.append('brandId', b); });
            }
            if (override.attrs !== undefined) {
                var curAttrs = p.getAll('attrs').slice();
                var attrVal = override.attrs;
                var i = curAttrs.indexOf(attrVal);
                if (i >= 0) curAttrs.splice(i, 1);
                else curAttrs.push(attrVal);
                p.delete('attrs');
                curAttrs.forEach(function (a) { p.append('attrs', a); });
            }
            if (override.skuPrice !== undefined) {
                if (!override.skuPrice) p.delete('skuPrice');
                else p.set('skuPrice', override.skuPrice);
            }
            if (override.sort !== undefined) {
                if (!override.sort) p.delete('sort');
                else p.set('sort', override.sort);
            }
            if (override.sortBtn !== undefined) {
                if (!override.sortBtn) p.delete('sortBtn');
                else p.set('sortBtn', override.sortBtn);
            }
            if (override.hasStock !== undefined) {
                if (override.hasStock == null || override.hasStock === '' || override.hasStock === 0) p.delete('hasStock');
                else p.set('hasStock', String(override.hasStock));
            }
        }
        if (keyword) p.set('keyword', keyword);
        var qs = p.toString();
        return (qs ? '?' + qs : window.location.pathname);
    }

    /** 带锚点的搜索链接，跳转后固定在搜索结果区域，避免回到页面顶部 */
    var SEARCH_ANCHOR = '#search-results-rig-tab';
    var SCROLL_POSITION_KEY = 'searchScrollPosition';
    function buildSearchHref(override) {
        var q = buildSearchUrl(override);
        var path = window.location.pathname;
        return (q.charAt(0) === '?' ? path + q : path) + SEARCH_ANCHOR;
    }
    /** 点击分页/筛选链接时保存当前滚动位置，以便新页加载后恢复 */
    function saveScrollPositionOnSearchNav(e) {
        var a = e.target && (e.target.closest ? e.target.closest('a') : $(e.target).closest('a')[0]);
        if (!a || !a.href) return;
        var href = a.getAttribute('href');
        if (!href || href === '#') return;
        try {
            var pathname = a.pathname || '';
            if (pathname === window.location.pathname && (href.indexOf('?') >= 0 || href.indexOf('pageNum') >= 0 || href.indexOf('brandId') >= 0 || href.indexOf('catalog3Id') >= 0 || href.indexOf('attrs') >= 0 || href.indexOf('hasStock') >= 0)) {
                sessionStorage.setItem(SCROLL_POSITION_KEY, String(window.scrollY));
                sessionStorage.setItem('searchNavInProgress', '1');
            }
        } catch (err) {}
    }
    /** 移除跳转遮罩并清除标志；若存在已保存的滚动位置则恢复 */
    function clearSearchNavOverlayAndRestoreScroll() {
        try {
            sessionStorage.removeItem('searchNavInProgress');
            var o = document.getElementById('search-nav-loading-overlay');
            if (o && o.parentNode) o.parentNode.removeChild(o);
            var saved = sessionStorage.getItem(SCROLL_POSITION_KEY);
            if (saved !== null && saved !== '') {
                sessionStorage.removeItem(SCROLL_POSITION_KEY);
                var y = parseInt(saved, 10);
                if (!isNaN(y) && y >= 0) {
                    setTimeout(function () { window.scrollTo(0, y); }, 0);
                }
            }
        } catch (err) {}
    }

    function renderDynamicFilters(data) {
        var brands = (data && data.brands) ? data.brands : [];
        var catalogs = (data && data.catalogs) ? data.catalogs : [];
        var attrs = (data && data.attrs) ? data.attrs : [];
        var html = '';

        if (catalogs.length > 0) {
            html += '<div class="JD_pre" style="margin-bottom:12px;"><div class="sl_key"><span>Category: </span></div><div class="sl_value"><ul>';
            catalogs.forEach(function (c) {
                var url = buildSearchHref({ catalog3Id: c.catalogId, pageNum: 1 });
                html += '<li><a href="' + url + '">' + (c.catalogName || c.catalogId) + '</a></li>';
            });
            html += '</ul></div></div>';
        }

        if (brands.length > 0) {
            var seenBrandIds = {};
            var uniqueBrands = brands.filter(function (b) {
                var id = b.brandId != null ? String(b.brandId) : '';
                if (!id || seenBrandIds[id]) return false;
                seenBrandIds[id] = true;
                return true;
            });
            html += '<div class="JD_pre" style="margin-bottom:12px;"><div class="sl_key"><span>Brand: </span></div><div class="sl_value"><ul class="sl_value_logo" style="list-style:none;padding:0;margin:0;">';
            uniqueBrands.forEach(function (b) {
                var url = buildSearchHref({ brandId: b.brandId, pageNum: 1 });
                html += '<li style="display:inline-block;margin:4px;"><a href="' + url + '" title="' + escapeHtml(b.brandName || b.brandId || '') + '">';
                if (b.brandImg) {
                    html += '<img src="' + escapeHtml(b.brandImg) + '" alt="" style="width:44px;height:28px;object-fit:contain;vertical-align:middle;" onerror="this.style.display=\'none\'">';
                } else {
                    html += '<span style="width:44px;height:28px;display:inline-block;vertical-align:middle;"></span>';
                }
                html += '</a></li>';
            });
            html += '</ul></div></div>';
        }

        if (attrs.length > 0) {
            attrs.forEach(function (attr) {
                var name = (attr.attrName || 'Attr') + ': ';
                var values = (attr.attrValue && attr.attrValue.length) ? attr.attrValue : [];
                if (values.length === 0) return;
                html += '<div class="JD_pre" style="margin-bottom:12px;"><div class="sl_key"><span>' + escapeHtml(name) + '</span></div><div class="sl_value"><ul style="list-style:none;padding:0;margin:0;">';
                values.forEach(function (v) {
                    var attrParam = (attr.attrId || '') + '_' + (v || '').trim();
                    if (!attrParam || attrParam === '_') return;
                    var url = buildSearchHref({ attrs: attrParam, pageNum: 1 });
                    html += '<li style="display:inline-block;margin:2px 6px 2px 0;"><a href="' + url + '">' + escapeHtml(v) + '</a></li>';
                });
                html += '</ul></div></div>';
            });
        }

        if (!html) {
            html = '<div class="search-filters-placeholder" style="padding:12px;color:#666;font-size:12px;">No filters for this search. Try another keyword.</div>';
        }
        var container = document.getElementById('search-filter-container');
        if (container) container.innerHTML = html;
    }

    /** 根据当前 sort、sortBtn 更新排序按钮：仅选中项显示箭头（▼/▲），点击切换升降序；sortBtn 指定哪个按钮高亮 */
    function updateSortButtonsActiveState() {
        var params = new URLSearchParams(location.search);
        var currentSort = params.get('sort') || '';
        if (!currentSort) currentSort = 'hotScore_desc';
        var currentSortBtn = params.get('sortBtn') || '';
        if (!currentSortBtn && currentSort) {
            var btnMap = { hotScore_desc: 'relevance', hotScore_asc: 'relevance', salecount_desc: 'sales', salecount_asc: 'newest', price_desc: 'price', price_asc: 'price' };
            currentSortBtn = btnMap[currentSort] || '';
        }
        $('#search-sort-btns .sort-btn').each(function () {
            var btn = $(this);
            var sortBtn = (btn.attr('data-sort-btn') || '').trim();
            var desc = (btn.attr('data-sort-desc') || '').trim();
            var asc = (btn.attr('data-sort-asc') || '').trim();
            var isActive = (sortBtn === currentSortBtn) && (currentSort === desc || currentSort === asc);
            var arrowSpan = btn.find('.sort-arrow');
            var targetSort, arrowChar;
            if (isActive) {
                if (currentSort === desc) {
                    targetSort = asc;
                    arrowChar = '\u25BC';
                } else {
                    targetSort = desc;
                    arrowChar = '\u25B2';
                }
                arrowSpan.text(' ' + arrowChar);
            } else {
                targetSort = desc;
                arrowSpan.text('');
            }
            btn.attr('href', buildSearchHref({ sort: targetSort, sortBtn: sortBtn, pageNum: 1 })).toggleClass('active', isActive);
        });
    }

    /**
     * 根据 total、当前页、pageSize 更新分页条：显示真实总页数，生成页码链接，Prev/Next、跳转输入框+OK
     */
    function renderPagination(total, currentPageNum, pageSize) {
        var totalPages = total <= 0 ? 1 : Math.ceil(total / pageSize);
        var current = parseInt(currentPageNum, 10) || 1;
        current = Math.max(1, Math.min(current, totalPages));

        $('#search-current-page').text(current);
        $('#search-total-page').text(totalPages);
        $('#search-total-pages-num').text(totalPages);

        var prevHref = current > 1 ? buildSearchHref({ pageNum: current - 1 }) : '#';
        var nextHref = current < totalPages ? buildSearchHref({ pageNum: current + 1 }) : '#';
        $('#search-prev').attr('href', prevHref).toggleClass('pagination-disabled', current <= 1);
        $('#search-next').attr('href', nextHref).toggleClass('pagination-disabled', current >= totalPages);

        var linksHtml = '';
        var prevDisabled = current <= 1;
        var nextDisabled = current >= totalPages;
        linksHtml += '<a href="' + (prevDisabled ? '#' : buildSearchHref({ pageNum: current - 1 })) + '" id="search-prev-link" class="page-prev' + (prevDisabled ? ' pagination-disabled' : '') + '">&lt; Prev</a>';
        for (var n = 1; n <= totalPages; n++) {
            if (totalPages > 7 && n > 3 && n < totalPages) {
                if (n === 4) linksHtml += '<a href="#" style="border:0;font-size:20px;color:#999;background:#fff;pointer-events:none">...</a>';
                continue;
            }
            var activeStyle = n === current ? ' style="border:0;color:#ee2222;background:#fff"' : '';
            linksHtml += '<a href="' + buildSearchHref({ pageNum: n }) + '"' + activeStyle + '>' + n + '</a>';
        }
        linksHtml += '<a href="' + (nextDisabled ? '#' : buildSearchHref({ pageNum: current + 1 })) + '" id="search-next-link" class="page-next' + (nextDisabled ? ' pagination-disabled' : '') + '">Next &gt;</a>';
        $('#search-page-links').html(linksHtml);

        var gotoInput = document.getElementById('search-goto-input');
        if (gotoInput) {
            gotoInput.value = current;
            gotoInput.setAttribute('min', '1');
            gotoInput.setAttribute('max', String(totalPages));
        }
        $('#search-goto-ok').off('click.pagination').on('click.pagination', function (e) {
            e.preventDefault();
            var num = parseInt(gotoInput.value, 10);
            if (isNaN(num) || num < 1) num = 1;
            if (num > totalPages) num = totalPages;
            window.location.href = buildSearchHref({ pageNum: num });
        });
    }

    function escapeHtml(s) {
        if (!s) return '';
        var div = document.createElement('div');
        div.textContent = s;
        return div.innerHTML;
    }

    function buildKeywordOnlySearchHref(keyword) {
        var normalizedKeyword = (keyword || '').trim();
        if (!normalizedKeyword) return window.location.pathname + SEARCH_ANCHOR;
        var params = new URLSearchParams();
        params.set('keyword', normalizedKeyword);
        return window.location.pathname + '?' + params.toString() + SEARCH_ANCHOR;
    }

    function renderBreadcrumb(data, keyword) {
        var container = document.querySelector(breadcrumbContainerSelector);
        if (!container) return;

        var normalizedKeyword = (keyword || '').trim();
        if (!normalizedKeyword && (!data || !data.navs || !data.navs.length)) {
            if (defaultBreadcrumbHtml) container.innerHTML = defaultBreadcrumbHtml;
            return;
        }

        var navs = (data && data.navs) ? data.navs : [];
        var html = '';
        var rightArrowHtml = '<i class="search-breadcrumb-sep"><img src="/search-page/image/right-@1x.png" alt=""></i>';

        if (normalizedKeyword) {
            html += '<div class="JD_ipone_one a search-breadcrumb-root">';
            html += '<a href="' + buildKeywordOnlySearchHref(normalizedKeyword) + '">' + escapeHtml(normalizedKeyword) + '</a>';
            html += '</div>';
        }

        if (navs.length > 0) {
            if (normalizedKeyword) html += rightArrowHtml;
            navs.forEach(function (nav, index) {
                var navName = (nav && nav.navName) ? nav.navName : 'Filter';
                var navValue = (nav && nav.navValue) ? nav.navValue : '';
                var navText = escapeHtml(navName) + ': ' + escapeHtml(navValue);
                var link = nav && nav.link ? String(nav.link).trim() : '';
                var hasCancelableLink = !!link;
                html += '<div class="JD_ipone_one search-breadcrumb-item">';
                if (hasCancelableLink) {
                    html += '<a href="' + escapeHtml(link) + '" class="search-breadcrumb-link" title="Remove this filter">';
                    html += '<span class="search-breadcrumb-label">' + navText + '</span>';
                    html += '<span class="search-breadcrumb-close">×</span>';
                    html += '</a>';
                } else {
                    html += '<span class="search-breadcrumb-current">' + navText + '</span>';
                }
                html += '</div>';
                if (index < navs.length - 1) {
                    html += rightArrowHtml;
                }
            });
        }

        container.innerHTML = html;
    }

    /** Strip HTML tags for use in title attribute (plain text). */
    function stripHtml(html) {
        if (!html) return '';
        var div = document.createElement('div');
        div.innerHTML = html;
        return (div.textContent || div.innerText || '').trim();
    }

    var PLACEHOLDER_IMG = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="205" height="210" viewBox="0 0 205 210"><rect fill="#f0f0f0" width="205" height="210"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#999" font-size="14" font-family="sans-serif">No image</text></svg>');
    var PLACEHOLDER_IMG_FALLBACK = '/search-page/image/logo1.jpg';

    /** Build one product card HTML to fit in a rig_tab slot (one div per result). */
    function buildProductCardHtml(item) {
        var skuId = item.skuId || '';
        var img = (item.skuImg && item.skuImg.trim()) ? item.skuImg.trim() : '';
        if (!img) img = PLACEHOLDER_IMG;
        var titleRaw = (item.skuTitle || '');
        var titleDisplay = titleRaw.length > 80 ? titleRaw.substring(0, 80) + '...' : titleRaw;
        var titlePlain = stripHtml(titleDisplay);
        var price = item.skuPrice != null ? item.skuPrice : '';
        // 商品详情：Next rewrite /{skuId}.html → /item/{skuId}（与 item 子域 SEO URL 一致；静态 CSS/JS 仍走 /static/item-detail/）
        var link =
            skuId !== '' && skuId != null
                ? '/' + encodeURIComponent(String(skuId)) + '.html'
                : '/search';
        var imgSrc = escapeHtml(img);
        var imgTag = '<img src="' + imgSrc + '" alt="" class="dim search-result-img" onerror="this.onerror=null;this.src=\'' + PLACEHOLDER_IMG_FALLBACK + '\'">';
        return '<div class="search-result-card-ico"><a href="' + link + '">详情</a></div>' +
            '<p class="da"><a href="' + link + '" title="' + escapeHtml(titlePlain) + '">' + imgTag + '</a></p>' +
            '<p class="tab_R price-inline"><span class="price-symbol">¥</span><span class="price-num">' + escapeHtml(String(price)) + '</span></p>' +
            '<p class="tab_JE"><a href="' + link + '" title="' + escapeHtml(titlePlain) + '">' + titleDisplay + '</a></p>';
    }

    function renderList() {
        var keyword = getQueryParam('keyword');
        var filterContainer = document.getElementById('search-filter-container');
        var rigTab = $('#search-results-rig-tab');

        rigTab.siblings('.rig_tab').hide();
        var params = new URLSearchParams(location.search);
        var pageNum = params.get('pageNum') || '1';
        var pageSize = params.get('pageSize') || '4';
        var catalog3Ids = params.getAll('catalog3Id').filter(Boolean);
        var sort = params.get('sort') || '';
        var hasStock = params.get('hasStock') || '';
        var skuPrice = params.get('skuPrice') || '';
        var brandIds = params.getAll('brandId');
        var attrsList = params.getAll('attrs').filter(Boolean);

        var query = {
            pageNum: pageNum,
            pageSize: pageSize
        };
        if (keyword && keyword.trim()) query.keyword = keyword.trim();
        if (catalog3Ids.length) query.catalog3Id = catalog3Ids;
        if (sort) query.sort = sort;
        if (hasStock) query.hasStock = hasStock;
        if (skuPrice) query.skuPrice = skuPrice;
        if (brandIds.length) query.brandId = brandIds;
        if (attrsList.length) query.attrs = attrsList;

        $.ajax({
            url: '/api/search/product/list',
            type: 'GET',
            // Serialize arrays as repeated keys (attrs=a&attrs=b) instead of attrs[]=a&attrs[]=b.
            // Backend SearchParam binds repeated keys but rejects bracket-style arrays.
            traditional: true,
            data: query,
            dataType: 'json',
            success: function (res) {
                var data = (res && res.data) ? res.data : null;
                var list = (data && data.products) ? data.products : [];
                var total = (data && data.total != null) ? Number(data.total) : 0;

                $('#productTotal').text(total);
                if (rigTab.length) {
                    rigTab.siblings('.rig_tab').hide();
                    rigTab.find('.search-result-item').remove();
                    rigTab.children().hide();
                    if (list.length === 0) {
                        var emptyDiv = $('<div class="search-result-item" style="padding:20px;color:#666;">No products found.</div>');
                        rigTab.append(emptyDiv);
                    } else {
                        for (var i = 0; i < list.length; i++) {
                            var div = $('<div class="search-result-item"></div>');
                            div.html(buildProductCardHtml(list[i]));
                            rigTab.append(div);
                        }
                    }
                }
                renderBreadcrumb(data, keyword);
                renderDynamicFilters(data);
                renderPagination(total, pageNum, parseInt(pageSize, 10) || 4);
                updateSortButtonsActiveState();
                initHasStockFilter();
                initPriceRangeFilter();
                clearSearchNavOverlayAndRestoreScroll();
            },
            error: function () {
                $('#productTotal').text('0');
                if (rigTab.length) {
                    rigTab.siblings('.rig_tab').hide();
                    rigTab.find('.search-result-item').remove();
                    rigTab.children().hide();
                    rigTab.append('<div class="search-result-item" style="padding:20px;color:#c00;">Load failed. Please try again.</div>');
                }
                if (filterContainer) {
                    filterContainer.innerHTML = '<div class="search-filters-placeholder" style="padding:12px;color:#999;">Filters unavailable.</div>';
                }
                renderBreadcrumb(null, keyword);
                renderPagination(0, 1, parseInt(pageSize, 10) || 4);
                clearSearchNavOverlayAndRestoreScroll();
            }
        });
    }

    function initHasStockFilter() {
        var params = new URLSearchParams(location.search);
        var hasStock = params.get('hasStock') || '';
        var checked = (hasStock === '1');
        var link = document.getElementById('search-hasstock-link');
        var li = document.getElementById('search-hasstock-li');
        if (link && li) {
            link.href = buildSearchHref({ hasStock: checked ? null : 1, pageNum: 1 });
            li.classList.toggle('hasstock-checked', checked);
        }
    }

    function initPriceRangeFilter() {
        var params = new URLSearchParams(location.search);
        var skuPrice = params.get('skuPrice') || '';
        var min = '', max = '';
        if (skuPrice && skuPrice.indexOf('_') >= 0) {
            var parts = skuPrice.split('_');
            if (parts[0]) min = parts[0];
            if (parts[1]) max = parts[1];
        }
        $('#search-price-min').val(min);
        $('#search-price-max').val(max);
    }
    function applyPriceRangeFilter() {
        var minVal = ($('#search-price-min').val() || '').trim();
        var maxVal = ($('#search-price-max').val() || '').trim();
        var skuPrice = '';
        if (minVal || maxVal) {
            skuPrice = (minVal || '') + '_' + (maxVal || '');
        }
        var url = buildSearchHref({ skuPrice: skuPrice || null, pageNum: 1 });
        sessionStorage.setItem(SCROLL_POSITION_KEY, String(window.scrollY));
        sessionStorage.setItem('searchNavInProgress', '1');
        window.location.href = url;
    }

    $(function () {
        var breadcrumbContainer = document.querySelector(breadcrumbContainerSelector);
        if (breadcrumbContainer) {
            defaultBreadcrumbHtml = breadcrumbContainer.innerHTML;
        }
        $(document).on('click', 'a', saveScrollPositionOnSearchNav);
        $('#search-price-ok').on('click', applyPriceRangeFilter);
        initPriceRangeFilter();
        initHasStockFilter();
        updateSortButtonsActiveState();
        renderList();
        var keywordInput = document.getElementById('keyword');
        if (keywordInput) {
            var params = new URLSearchParams(location.search);
            keywordInput.value = params.get('keyword') || '';
        }
    });
})();
