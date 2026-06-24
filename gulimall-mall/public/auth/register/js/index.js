$(function () {
    var AUTH_API_BASE = (typeof window.GULIMALL_AUTH_API === 'string' && window.GULIMALL_AUTH_API) || '/api/auth';
    var smsCooldownTimer = null;
    var stuList = getStuList();

    function validSmsMobile(v) {
        var mode = $('#smsCountry').val();
        var s = (v || '').trim();
        if (mode === 'china') {
            return /^1[3-9]\d{9}$/.test(s);
        }
        var d = s.replace(/\D/g, '');
        if (d.length === 11 && d.charAt(0) === '1') {
            d = d.substring(1);
        }
        if (d.length !== 10) {
            return false;
        }
        return /^[2-9]\d{2}[2-9]\d{2}\d{4}$/.test(d);
    }

    function updatePhonePlaceholder() {
        var $p = $('#reg-phone');
        if ($('#smsCountry').val() === 'china') {
            $p.attr('placeholder', 'e.g. 13800138000');
        } else {
            $p.attr('placeholder', 'e.g. 8259493401');
        }
    }

    $('#smsCountry').on('change', updatePhonePlaceholder);
    updatePhonePlaceholder();

    function smsFieldTips($input) {
        return $input.closest('.register-box').children('.tips').first();
    }

    $('input').eq(0).focus(function () {
        if ($(this).val().length === 0) {
            $(this).parent().next('div').text('Use letters, numbers, hyphen or underscore (4–20 characters).');
        }
    });
    $('input').eq(1).focus(function () {
        if ($(this).val().length === 0) {
            $(this).parent().next('div').text('At least 6 characters; mixing letters and numbers is recommended.');
        }
    });
    $('input').eq(2).focus(function () {
        if ($(this).val().length === 0) {
            $(this).parent().next('div').text('Re-enter the same password.');
        }
    });
    $('input').eq(3).focus(function () {
        if ($(this).val().length === 0) {
            $(this).parent().next('div').text('Used for SMS verification, sign-in, and account recovery.');
        }
    });
    $('input').eq(4).focus(function () {
        if ($(this).val().length === 0) {
            smsFieldTips($(this)).text('Tap Send first, then enter the 6-digit code.');
        }
    });

    $('input').eq(0).blur(function () {
        if ($(this).val().length === 0) {
            $(this).parent().next('div').text('').css('color', '#ccc');
        } else if ($(this).val().length > 0 && $(this).val().length < 4) {
            $(this).parent().next('div').text('Username must be 4–20 characters.').css('color', 'red');
        } else if ($(this).val().length >= 4 && !isNaN($(this).val())) {
            $(this).parent().next('div').text('Username cannot be all numbers.').css('color', 'red');
        } else {
            for (var m = 0; m < stuList.length; m++) {
                if ($(this).val() === stuList[m].name) {
                    $(this).parent().next('div').text('That username is already taken.').css('color', 'red');
                    return;
                }
            }
            $(this).parent().next('div').text('');
        }
    });

    $('input').eq(1).blur(function () {
        if ($(this).val().length === 0) {
            $(this).parent().next('div').text('').css('color', '#ccc');
        } else if ($(this).val().length > 0 && $(this).val().length < 6) {
            $(this).parent().next('div').text('Password must be at least 6 characters.').css('color', 'red');
        } else {
            $(this).parent().next('div').text('');
        }
    });

    $('input').eq(2).blur(function () {
        if ($(this).val().length === 0) {
            $(this).parent().next('div').text('').css('color', '#ccc');
        } else if ($(this).val() !== $('input').eq(1).val()) {
            $(this).parent().next('div').text('Passwords do not match.').css('color', 'red');
        } else {
            $(this).parent().next('div').text('');
        }
    });

    $('input').eq(3).blur(function () {
        if ($(this).val().length === 0) {
            $(this).parent().next('div').text('').css('color', '#ccc');
        } else if (!validSmsMobile($(this).val())) {
            $(this).parent().next('div').text('Invalid phone number for the selected country.').css('color', 'red');
        } else {
            $(this).parent().next('div').text('');
        }
    });

    function startSmsCooldown(sec) {
        var left = sec;
        var $btn = $('#sendSmsBtn');
        $btn.prop('disabled', true);
        if (smsCooldownTimer) {
            clearInterval(smsCooldownTimer);
        }
        function tick() {
            if (left <= 0) {
                clearInterval(smsCooldownTimer);
                smsCooldownTimer = null;
                $btn.prop('disabled', false).text('Send');
                return;
            }
            $btn.text('Resend in ' + left + 's');
            left--;
        }
        tick();
        smsCooldownTimer = setInterval(tick, 1000);
    }

    $('#sendSmsBtn').click(function () {
        var mobile = ($('input').eq(3).val() || '').trim();
        if (!validSmsMobile(mobile)) {
            $('input').eq(3).parent().next('div').text('Enter a valid mobile number.').css('color', 'red');
            return;
        }
        $.ajax({
            url: AUTH_API_BASE + '/sms/send',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ mobile: mobile }),
            success: function (data) {
                if (data.code === 0) {
                    startSmsCooldown(60);
                } else {
                    alert(data.msg || 'Failed to send code');
                    if (data.msg && data.msg.indexOf('60 seconds') >= 0) {
                        startSmsCooldown(60);
                    }
                }
            },
            error: function () {
                alert('Network error');
            }
        });
    });

    $('input').eq(4).blur(function () {
        var v = $(this).val().trim();
        var $tips = smsFieldTips($(this));
        if (v.length === 0) {
            $tips.text('').css('color', '#ccc');
        } else if (!/^\d{6}$/.test(v)) {
            $tips.text('Enter the 6-digit code.').css('color', 'red');
        } else {
            $tips.text('');
        }
    });

    /** Same rules as blur handlers; call before register AJAX. */
    function validateRegisterFormForSubmit() {
        var $inp = $('form.one input');
        function rowTips(i) {
            if (i === 4) {
                return smsFieldTips($inp.eq(4));
            }
            return $inp.eq(i).parent().next('.tips');
        }
        var user = $inp.eq(0).val();
        var pass = $inp.eq(1).val();
        var pass2 = $inp.eq(2).val();
        var mobile = $inp.eq(3).val().trim();
        var code = $inp.eq(4).val().trim();

        if (user.length < 4 || user.length > 20) {
            rowTips(0).text('Username must be 4–20 characters.').css('color', 'red');
            $inp.eq(0).focus();
            return false;
        }
        if (user.length >= 4 && !isNaN(user)) {
            rowTips(0).text('Username cannot be all numbers.').css('color', 'red');
            $inp.eq(0).focus();
            return false;
        }
        var si;
        for (si = 0; si < stuList.length; si++) {
            if (user === stuList[si].name) {
                rowTips(0).text('That username is already taken.').css('color', 'red');
                $inp.eq(0).focus();
                return false;
            }
        }
        if (pass.length < 6) {
            rowTips(1).text('Password must be at least 6 characters.').css('color', 'red');
            $inp.eq(1).focus();
            return false;
        }
        if (pass !== pass2) {
            rowTips(2).text('Passwords do not match.').css('color', 'red');
            $inp.eq(2).focus();
            return false;
        }
        if (!validSmsMobile(mobile)) {
            rowTips(3).text('Invalid phone number for the selected country.').css('color', 'red');
            $inp.eq(3).focus();
            return false;
        }
        if (!/^\d{6}$/.test(code)) {
            rowTips(4).text('Enter the 6-digit code.').css('color', 'red');
            $inp.eq(4).focus();
            return false;
        }
        return true;
    }

    $('#submit_btn').click(function (e) {
        e.preventDefault();
        for (var j = 0; j < 5; j++) {
            if ($('input').eq(j).val().length === 0) {
                $('input').eq(j).focus();
                if (j === 4) {
                    smsFieldTips($('input').eq(j)).text('This field is required.').css('color', 'red');
                    return;
                }
                $('input').eq(j).parent().next('.tips').text('This field is required.').css('color', 'red');
                return;
            }
        }
        if (!$('#xieyi')[0].checked) {
            $('#xieyi').next().next().next('.tips').text('Please accept the terms.').css('color', 'red');
            return;
        }
        if (!validateRegisterFormForSubmit()) {
            return;
        }
        var payload = {
            username: $('input').eq(0).val(),
            password: $('input').eq(1).val(),
            mobile: $('input').eq(3).val().trim(),
            smsCode: $('input').eq(4).val().trim()
        };
        $.ajax({
            url: AUTH_API_BASE + '/register',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function (data) {
                if (data.code === 0) {
                    alert('Registration successful');
                    window.location.href = '/login';
                } else {
                    alert(data.msg || 'Registration failed');
                }
            },
            error: function () {
                alert('Network error');
            }
        });
    });

    function Student(name, password, tel, id) {
        this.name = name;
        this.password = password;
        this.tel = tel;
        this.id = id;
    }

    function getStuList() {
        var list = localStorage.getItem('stuList');
        if (list != null) {
            return JSON.parse(list);
        }
        return [];
    }
});
