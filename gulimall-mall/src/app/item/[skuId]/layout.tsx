export default function ItemDetailLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <link rel="stylesheet" href="/static/item-detail/scss/shop.css?v=20260521gallery" />
      {children}
    </>
  );
}
