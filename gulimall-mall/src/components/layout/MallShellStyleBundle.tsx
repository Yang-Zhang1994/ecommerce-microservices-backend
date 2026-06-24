import styles from './MallShell.module.css';

/** Keeps MallShell CSS in the root layout chunk (header styles always load). */
export default function MallShellStyleBundle() {
  void styles;
  return null;
}
