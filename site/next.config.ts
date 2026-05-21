import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  // Turbopack может резолвить сгенерированный клиент под edge-light и тянуть wasm (нужен prisma://).
  // Webpack + внешний пакет дают обычный Node-движок и mysql:// из DATABASE_URL.
  serverExternalPackages: ["@prisma/client", "prisma", ".prisma/client"],
};

export default nextConfig;
