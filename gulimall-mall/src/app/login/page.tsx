import { Suspense } from 'react';
import MallShell from '@/components/layout/MallShell';
import LoginForm from './LoginForm';
import styles from '../auth/auth.module.css';

export const metadata = {
  title: 'Sign in',
};

/** Avoid stale prerendered HTML pointing at old /_next/static hashes after redeploy. */
export const dynamic = 'force-dynamic';

export default function LoginPage() {
  return (
    <MallShell showSearch={false}>
      <div className={styles.wrap}>
        <Suspense fallback={<p>Loading…</p>}>
          <LoginForm />
        </Suspense>
      </div>
    </MallShell>
  );
}
