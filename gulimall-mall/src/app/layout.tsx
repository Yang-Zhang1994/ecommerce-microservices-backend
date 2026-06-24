import type { Metadata } from 'next';
import AuthSessionSync from '@/components/AuthSessionSync';
import MallShellStyleBundle from '@/components/layout/MallShellStyleBundle';
import './globals.css';

import { BRAND_NAME } from '@/lib/brand';

export const metadata: Metadata = {
  title: { default: BRAND_NAME, template: `%s · ${BRAND_NAME}` },
  description: `${BRAND_NAME} — online storefront`,
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <MallShellStyleBundle />
        <AuthSessionSync />
        {children}
      </body>
    </html>
  );
}
