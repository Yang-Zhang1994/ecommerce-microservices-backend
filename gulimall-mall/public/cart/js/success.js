// success.html: verify skuId exists in server-side cart before showing success UI (tampered URLs fail).
(function () {
    function getParam(name) {
        var sp = new URLSearchParams(window.location.search || "");
        return sp.get(name);
    }

    function safeText(v) {
        return (v == null || v === "") ? null : String(v);
    }

    function decodeParam(raw) {
        if (raw == null) return null;
        var s = String(raw).replace(/\+/g, " ");
        try {
            return decodeURIComponent(s);
        } catch (e) {
            return s;
        }
    }

    var titleEl = document.getElementById("successTitle");
    var skuNameEl = document.getElementById("successSkuName");
    var skuNumEl = document.getElementById("successSkuNum");
    var skuImgEl = document.getElementById("successSkuImg");
    var detailBtn = document.getElementById("viewSkuDetailBtn");
    var resultEl = document.getElementById("result");
    var pItemRow = document.querySelector(".success-lcol .p-item");
    var btnRow = document.querySelector(".success-btns");

    function replaceWithMessage(heading, detailHtml) {
        if (!resultEl) return;
        resultEl.innerHTML =
            '<div class="cart-verify-panel" style="padding:48px 24px;text-align:center;background:#fff;border:1px solid #eee;">' +
            '<p style="font-size:48px;line-height:1;margin-bottom:16px;color:#f90;">!</p>' +
            '<h3 class="ftx-02" style="margin-bottom:12px;font-size:20px;">' +
            heading +
            "</h3>" +
            (detailHtml || "") +
            '<p style="margin-top:24px;"><a href="/" style="color:#e4393c;font-size:16px;">Back to store home</a>' +
            ' &nbsp;|&nbsp; <a href="/cart/list" style="color:#005ea7;font-size:16px;">Go to cart</a></p>' +
            "</div>";
    }

    function isPlaceholderProductName(text) {
        if (!text) return true;
        var t = String(text).trim();
        // TCL + legacy demo copy (JD / mall name in older templates)
        return (
            t.indexOf("TCL") >= 0 ||
            t.indexOf("\u4eac\u4e1c") >= 0 ||
            t.indexOf("\u8c37\u7c92") >= 0
        );
    }

    function stripThymeleafAttrs(el) {
        if (!el || !el.attributes) return;
        var toRemove = [];
        for (var i = 0; i < el.attributes.length; i++) {
            var n = el.attributes[i].name;
            if (n.indexOf("th:") === 0) {
                toRemove.push(n);
            }
        }
        for (var j = 0; j < toRemove.length; j++) {
            el.removeAttribute(toRemove[j]);
        }
    }

    var errParam = safeText(getParam("err"));
    if (errParam === "invalidSku") {
        replaceWithMessage("Unable to add to cart", '<p style="color:#666;">Invalid SKU. Please choose again from the product page.</p>');
        return;
    }

    var skuId = safeText(getParam("skuId"));
    var numRaw = safeText(getParam("num"));
    var title = safeText(getParam("title"));
    var img = safeText(getParam("img"));

    var qtyFromUrl = 1;
    if (numRaw != null) {
        var parsedQty = parseInt(numRaw, 10);
        if (!isNaN(parsedQty) && parsedQty > 0) {
            qtyFromUrl = parsedQty;
        }
    }

    if (!skuId) {
        replaceWithMessage("Invalid link", '<p style="color:#666;">Missing product ID. Please add to cart again from the product page.</p>');
        return;
    }

    function fillFromSkuInfoApi(id) {
        if (!id) return;
        fetch("/api/product/skuinfo/info/" + encodeURIComponent(id), { credentials: "include" })
            .then(function (res) {
                if (!res.ok) throw new Error("HTTP " + res.status);
                return res.json();
            })
            .then(function (payload) {
                if (!payload || Number(payload.code) !== 0 || !payload.skuInfo) {
                    return;
                }
                var info = payload.skuInfo || {};
                if (skuNameEl && isPlaceholderProductName(skuNameEl.textContent)) {
                    var apiTitle = info.skuTitle || info.skuName;
                    if (apiTitle) {
                        skuNameEl.textContent = String(apiTitle);
                    }
                }
                var srcNow = skuImgEl ? (skuImgEl.getAttribute("src") || "") : "";
                if (skuImgEl && (!img || srcNow.indexOf("shop1.jpg") >= 0 || srcNow === "")) {
                    if (info.skuDefaultImg) {
                        skuImgEl.setAttribute("src", String(info.skuDefaultImg));
                    }
                }
            })
            .catch(function () {});
    }

    function applySuccessUi(cartItem) {
        var qty = qtyFromUrl;
        if (cartItem && cartItem.count != null && cartItem.count > 0) {
            qty = cartItem.count;
        }

        var displayTitle = null;
        if (cartItem && cartItem.title) {
            displayTitle = String(cartItem.title);
        }
        if (!displayTitle && title != null) {
            displayTitle = decodeParam(title);
        }

        var displayImg = null;
        if (cartItem && cartItem.image) {
            displayImg = String(cartItem.image);
        }

        if (skuNumEl) {
            stripThymeleafAttrs(skuNumEl);
            skuNumEl.textContent = "Quantity: " + qty;
        }

        if (titleEl) {
            titleEl.textContent = "Item added to cart successfully (SKU " + skuId + ")";
        }
        var detailUrl = "/item/" + encodeURIComponent(skuId);
        if (skuNameEl) {
            stripThymeleafAttrs(skuNameEl);
            if (displayTitle != null && displayTitle !== "") {
                skuNameEl.textContent = displayTitle;
            } else if (isPlaceholderProductName(skuNameEl.textContent)) {
                skuNameEl.textContent = "SKU " + skuId;
            }
            skuNameEl.setAttribute("href", detailUrl);
        }
        if (detailBtn) {
            stripThymeleafAttrs(detailBtn);
            detailBtn.setAttribute("href", detailUrl);
        }

        if (displayImg != null && displayImg !== "" && skuImgEl) {
            stripThymeleafAttrs(skuImgEl);
            skuImgEl.setAttribute("src", displayImg);
        } else if (img != null && skuImgEl) {
            stripThymeleafAttrs(skuImgEl);
            skuImgEl.setAttribute("src", decodeParam(img));
        }

        if (pItemRow) pItemRow.style.visibility = "visible";
        if (btnRow) btnRow.style.visibility = "visible";

        if (
            skuId != null &&
            (title == null || img == null || isPlaceholderProductName(skuNameEl && skuNameEl.textContent))
        ) {
            fillFromSkuInfoApi(skuId);
        }
    }

    if (titleEl) {
        titleEl.textContent = "Verifying cart…";
    }
    if (pItemRow) pItemRow.style.visibility = "hidden";
    if (btnRow) btnRow.style.visibility = "hidden";

    fetch("/api/cart/current", { credentials: "include" })
        .then(function (res) {
            if (!res.ok) throw new Error("HTTP " + res.status);
            return res.json();
        })
        .then(function (payload) {
            if (!payload || Number(payload.code) !== 0 || payload.data == null) {
                replaceWithMessage(
                    "Unable to verify cart",
                    '<p style="color:#666;">The server did not return cart data. Do not manually edit the URL parameters.</p>'
                );
                return;
            }
            var items = payload.data.items;
            if (!Array.isArray(items)) {
                items = [];
            }
            var match = null;
            for (var i = 0; i < items.length; i++) {
                if (String(items[i].skuId) === String(skuId)) {
                    match = items[i];
                    break;
                }
            }
            if (!match) {
                replaceWithMessage(
                    "Item not in cart",
                    '<p style="color:#666;">This link does not match your cart (for example, the SKU was edited manually). Please use the normal add-to-cart flow.</p>'
                );
                return;
            }
            applySuccessUi(match);
        })
        .catch(function () {
            replaceWithMessage(
                "Unable to verify cart",
                '<p style="color:#666;">Cannot reach the cart service or you may not be signed in, so this link cannot be verified. Do not manually edit the success page URL.</p>'
            );
        });
})();

// Dropdown menus (requires jQuery)
if (typeof jQuery !== "undefined") {
    jQuery(function () {
        $(".hd_wrap_left>.dorpdown").mouseover(function () {
            $(this).children(".dd").show();
            $(this).css("background", "#fff");

        }).mouseout(function () {
            $(this).children(".dd").hide();

            $(this).css("background", "#E3E4E5")

        });
        $(".hd_wrap_right>.hd_dj").mouseover(function () {
            $(this).children(".hd_dj_ol").show();
            $(this).css("background", "#fff");
        }).mouseout(function () {
            $(this).children(".hd_dj_ol").hide();
            $(this).css("background", "#E3E4E5");
        });
        $(".hd_wrap_right>.hd_kh").mouseover(function () {
            $(this).children(".hd_kh_ol").show();
            $(this).css("background", "#fff");
        }).mouseout(function () {
            $(this).children(".hd_kh_ol").hide();
            $(this).css("background", "#E3E4E5");
        });
        $(".hd_wrap_right>.hd_daohang").mouseover(function () {
            $(this).children(".hd_dh").show();
            $(this).css("background", "#fff");
        }).mouseout(function () {
            $(this).children(".hd_dh").hide();
            $(this).css("background", "#E3E4E5");
        });
        $(".hd_wrap_right>.hd_jing").mouseover(function () {
            $(this).children(".hd_jing_sj").show()
        }).mouseout(function () {
            $(this).children(".hd_jing_sj").hide()
        });
        $(".nav_top_three").mouseover(function () {
            $.get("/miniCart", function (data) {
                $(".dorpdown-layer").remove();
                $(".nav_top_three").append(data);

            })

            // $(this).children(".dorpdown-layer").show()
            // $(this).css("background", "#fff");
        }).mouseout(function () {
            $(this).children(".dorpdown-layer").hide()
        });

        $(".nav_down_ul_1").mouseover(function () {
            $(this).children(".nav_down_ul_ol").show();
        }).mouseout(function () {
            $(this).children(".nav_down_ul_ol").hide();
        })
        $(".li-1").mouseover(function () {
            $(this).children("#fen").show();
        }).mouseout(function () {
            $(this).children("#fen").hide();
        })
        $(".list_goods>.list_cover>a").mouseover(function () {
            $(this).children(".a_ol").show();
        }).mouseout(function () {
            $(this).children(".a_ol").hide();
        })
        $(".glyphicon-share").mouseover(function () {
            $(this).children(".wang").show();
        }).mouseout(function () {
            $(this).children(".wang").hide();
        })

    });
}


window.onload = function () {
    if (typeof jQuery === "undefined" || typeof Swiper === "undefined") {
        return;
    }
    try {
        var mySwiper = new Swiper('.banner1', {
            loop: true,
            pagination: '.swiper-pagination',
            paginationType: 'custom',
            paginationCustomRender: function (swiper, current, total) {
                var _html = '';
                for (var i = 1; i <= total; i++) {
                    if (current === i) {
                        _html += '<li class="swiper-pagination-custom active llll">' + i + '</li>';
                    } else {
                        _html += '<li class="swiper-pagination-custom llll">' + i + '</li>';
                    }
                }
                return _html;
            }

        })
        $('.banner1').on('mouseover', 'li', function () {
            var index = this.innerHTML;
            mySwiper.slideTo(index, 500, false);
        })


        var Banner = new Swiper('.banner', {
            loop: true,
            pagination: '.swiper-pagination',
            paginationType: 'custom',
            paginationCustomRender: function (swiper, current, total) {
                var _html = '';
                for (var i = 1; i <= total; i++) {
                    if (current === i) {
                        _html += '<li class="swiper-pagination-custom active la">' + i + '</li>';
                    } else {
                        _html += '<li class="swiper-pagination-custom la">' + i + '</li>';
                    }
                }
                return _html;
            }

        })
        $('.banner').on('mouseover', 'li', function () {
            var index = this.innerHTML;
            Banner.slideTo(index, 500, false);
        })
    } catch (e) {
        // success.html has no .banner/.banner1 — Swiper init may fail; ignore.
    }

}

