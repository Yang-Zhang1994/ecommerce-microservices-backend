'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { FormEvent, useEffect, useState } from 'react';
import {
  isValidRegisterMobile,
  phonePlaceholder,
  type PhoneCountry,
} from '@/lib/registerPhone';
import styles from '../auth/auth.module.css';

const SMS_COOLDOWN_SEC = 60;

export default function RegisterForm() {
  const router = useRouter();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [phoneCountry, setPhoneCountry] = useState<PhoneCountry>('nanp');
  const [mobile, setMobile] = useState('');
  const [smsCode, setSmsCode] = useState('');
  const [agree, setAgree] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [smsSending, setSmsSending] = useState(false);
  const [smsCooldown, setSmsCooldown] = useState(0);

  useEffect(() => {
    if (smsCooldown <= 0) return;
    const t = setInterval(() => {
      setSmsCooldown((s) => (s <= 1 ? 0 : s - 1));
    }, 1000);
    return () => clearInterval(t);
  }, [smsCooldown]);

  const sendSms = async () => {
    setError('');
    const phone = mobile.trim();
    if (!isValidRegisterMobile(phone, phoneCountry)) {
      setError('Enter a valid mobile number for the selected country.');
      return;
    }
    setSmsSending(true);
    try {
      const res = await fetch('/api/auth/sms/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ mobile: phone }),
      });
      const data = (await res.json()) as { code?: number; msg?: string };
      if (res.ok && data.code === 0) {
        setSmsCooldown(SMS_COOLDOWN_SEC);
        return;
      }
      const msg = data.msg || 'Failed to send code';
      setError(msg);
      if (msg.includes('60 seconds')) {
        setSmsCooldown(SMS_COOLDOWN_SEC);
      }
    } catch {
      setError('Network error while sending code');
    } finally {
      setSmsSending(false);
    }
  };

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    if (!username.trim() || !password || !mobile.trim() || !smsCode.trim()) {
      setError('Please fill in all fields');
      return;
    }
    if (!isValidRegisterMobile(mobile, phoneCountry)) {
      setError('Enter a valid mobile number for the selected country.');
      return;
    }
    if (password !== confirm) {
      setError('Passwords do not match');
      return;
    }
    if (!/^\d{6}$/.test(smsCode.trim())) {
      setError('SMS code must be 6 digits');
      return;
    }
    if (!agree) {
      setError('Please accept the terms');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: username.trim(),
          password,
          mobile: mobile.trim(),
          smsCode: smsCode.trim(),
        }),
      });
      const data = (await res.json()) as { code?: number; msg?: string };
      if (res.ok && data.code === 0) {
        router.push('/login');
        return;
      }
      setError(data.msg || 'Registration failed');
    } catch {
      setError('Network error');
    } finally {
      setLoading(false);
    }
  };

  const smsBtnDisabled = loading || smsSending || smsCooldown > 0;
  const smsBtnLabel =
    smsCooldown > 0 ? `Resend in ${smsCooldown}s` : smsSending ? 'Sending…' : 'Send code';

  return (
    <div className={styles.card}>
      <h1 className={styles.title}>Create account</h1>
      <p className={styles.subtitle}>Register to place orders and track payments.</p>

      <form className={styles.form} onSubmit={onSubmit}>
        {error && <p className={styles.error}>{error}</p>}
        <label className={styles.label}>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} disabled={loading} />
        </label>
        <label className={styles.label}>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={loading}
          />
        </label>
        <label className={styles.label}>
          Confirm password
          <input
            type="password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            disabled={loading}
          />
        </label>
        <label className={styles.label}>
          Mobile
          <span className={styles.phoneRow}>
            <select
              className={styles.phoneCountry}
              value={phoneCountry}
              onChange={(e) => setPhoneCountry(e.target.value as PhoneCountry)}
              disabled={loading}
              aria-label="Country calling code"
            >
              <option value="nanp">+1</option>
              <option value="china">+86</option>
            </select>
            <input
              className={styles.phoneInput}
              type="tel"
              placeholder={phonePlaceholder(phoneCountry)}
              value={mobile}
              onChange={(e) => setMobile(e.target.value)}
              disabled={loading}
              autoComplete="tel-national"
            />
          </span>
        </label>
        <div className={styles.smsRow}>
          <label className={`${styles.label} ${styles.smsCodeField}`}>
            SMS code
            <input
              inputMode="numeric"
              maxLength={6}
              placeholder="6 digits"
              value={smsCode}
              onChange={(e) => setSmsCode(e.target.value)}
              disabled={loading}
              autoComplete="one-time-code"
            />
          </label>
          <button
            type="button"
            className={styles.smsSendBtn}
            onClick={() => void sendSms()}
            disabled={smsBtnDisabled}
          >
            {smsBtnLabel}
          </button>
        </div>
        <p className={styles.smsHint}>Tap Send code first, then enter the 6-digit SMS verification code.</p>
        <label className={styles.checkbox}>
          <input type="checkbox" checked={agree} onChange={(e) => setAgree(e.target.checked)} disabled={loading} />
          <span>I agree to the GrainMart terms of service</span>
        </label>
        <button type="submit" className={styles.primaryBtn} disabled={loading}>
          {loading ? 'Registering…' : 'Register'}
        </button>
      </form>

      <p className={styles.footer}>
        Already have an account? <Link href="/login">Sign in</Link>
      </p>
    </div>
  );
}
