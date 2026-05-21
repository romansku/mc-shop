import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { cdnCommon } from "@/lib/cdnImages";
import Footer from "./component/Footer";
import Header from "./component/Header";
import "./globals.css";
import { CartStateProvider } from "./state/cartState";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Rift MC",
  description: "Rift MC - уютное ванильное выживание без Pay2Win и вайпов.",
  icons: {
    icon: [
      { url: cdnCommon("server-icon.png"), type: "image/png", sizes: "any" },
      {
        url: `${cdnCommon("server-icon.png")}?v=3`,
        type: "image/png",
        sizes: "32x32",
      },
    ],
    shortcut: [
      { url: `${cdnCommon("server-icon.png")}?v=3`, type: "image/png" },
    ],
    apple: [
      { url: `${cdnCommon("server-icon.png")}?v=3`, type: "image/png" },
    ],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">
        <CartStateProvider>
          <Header />
          {children}
          <Footer />
        </CartStateProvider>
      </body>
    </html>
  );
}
