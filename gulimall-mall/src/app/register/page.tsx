import MallShell from '@/components/layout/MallShell';
import RegisterForm from './RegisterForm';
import styles from '../auth/auth.module.css';

export const metadata = {
  title: 'Register',
};

export default function RegisterPage() {
  return (
    <MallShell showSearch={false}>
      <div className={styles.wrap}>
        <RegisterForm />
      </div>
    </MallShell>
  );
}
