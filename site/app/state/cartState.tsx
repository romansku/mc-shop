"use client";

import { createContext, useContext, useEffect, useMemo, useState } from "react";
import type { GoodsCard } from "@/app/dao/goodsDao";

const STORAGE_KEY = "minecraft-store-cart-v1";

export type CartItem = GoodsCard;

type CartStateValue = {
  items: CartItem[];
  total: number;
  addItem: (item: GoodsCard) => void;
  removeItem: (id: number) => void;
  clearCart: () => void;
  hasItem: (id: number) => boolean;
};

const CartStateContext = createContext<CartStateValue | null>(null);

function parseItems(value: string | null): CartItem[] {
  if (!value) {
    return [];
  }

  try {
    const parsed = JSON.parse(value) as unknown;
    if (!Array.isArray(parsed)) {
      return [];
    }

    return parsed.filter((item): item is CartItem => {
      return (
        typeof item === "object" &&
        item !== null &&
        typeof (item as { id?: unknown }).id === "number" &&
        typeof (item as { name?: unknown }).name === "string" &&
        typeof (item as { price?: unknown }).price === "number"
      );
    });
  } catch {
    return [];
  }
}

export function CartStateProvider({ children }: { children: React.ReactNode }) {
  const [items, setItems] = useState<CartItem[]>(() => {
    if (typeof window === "undefined") {
      return [];
    }

    return parseItems(window.localStorage.getItem(STORAGE_KEY));
  });

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }, [items]);

  const value = useMemo<CartStateValue>(() => {
    return {
      items,
      total: items.reduce((sum, item) => sum + item.price, 0),
      addItem: (item) =>
        setItems((current) => {
          if (current.some((currentItem) => currentItem.id === item.id)) {
            return current;
          }

          return [...current, item];
        }),
      removeItem: (id) =>
        setItems((current) => current.filter((item) => item.id !== id)),
      clearCart: () => setItems([]),
      hasItem: (id) => items.some((item) => item.id === id),
    };
  }, [items]);

  return (
    <CartStateContext.Provider value={value}>{children}</CartStateContext.Provider>
  );
}

export function useCartState() {
  const context = useContext(CartStateContext);

  if (!context) {
    throw new Error("useCartState must be used within CartStateProvider");
  }

  return context;
}
