/**
 * 搜索页：根据 URL 参数 keyword 请求 /api/search/product/list，渲染商品列表（动静分离：静态页由 Nginx 返回，接口经网关到 gulimall-search）
 */
(function () {
    function getQueryParam(name) {
        var params = new URLSearchParams(location.search);
        return params.get(name) || '';
    }

    function renderList(keyword) {
        if (!keyword) {
            $('#productTotal').text('0');
            $('#search-result-list').empty();
            return;
        }
        $.ajax({
            url: '/api/search/product/list',
            type: 'GET',
            data: { keyword: keyword, pageNum: 1, pageSize: 20 },
            dataType: 'json',
            success: function (res) {
                var total = (res && res.total) ? res.total : 0;
                var list = (res && res.data) ? res.data : [];
                $('#productTotal').text(total);
                var $container = $('#search-result-list');
                $container.empty();
                if (list.length === 0) {
                    $container.html('<p style="padding:20px;color:#666;">暂无相关商品</p>');
                    return;
                }
                var html = '<div class="search-product-grid" style="display:flex;flex-wrap:wrap;gap:16px;padding:16px 0;">';
                for (var i = 0; i < list.length; i++) {
                    var item = list[i];
                    var skuId = item.skuId || '';
                    var img = item.skuImg || '';
                    var title = (item.skuTitle || '').substring(0, 60);
                    var price = item.skuPrice != null ? item.skuPrice : '';
                    var link =
                        skuId !== '' && skuId != null
                            ? '/' + encodeURIComponent(String(skuId)) + '.html'
                            : '/search';
                    html += '<div class="search-product-item" style="width:180px;border:1px solid #eee;padding:8px;border-radius:4px;">';
                    html += '<a href="' + link + '" style="display:block;">';
                    html += '<img src="' + img + '" alt="" style="width:100%;height:180px;object-fit:contain;background:#f5f5f5;">';
                    html += '<p style="margin:8px 0;font-size:12px;line-height:1.4;color:#333;">' + title + '</p>';
                    html += '<p style="color:#e4393c;font-size:16px;">¥ ' + price + '</p>';
                    html += '</a></div>';
                }
                html += '</div>';
                $container.html(html);
            },
            error: function () {
                $('#productTotal').text('0');
                $('#search-result-list').html('<p style="padding:20px;color:#c00;">加载失败，请重试</p>');
            }
        });
    }

    $(function () {
        var keyword = getQueryParam('keyword');
        renderList(keyword);
    });
})();
