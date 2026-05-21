import { cdnCommon } from "@/lib/cdnImages";

const serverIcon = cdnCommon("server-icon.png");

export default function Head() {
  return (
    <>
      <link rel="icon" href={`${serverIcon}?v=3`} type="image/png" />
      <link rel="shortcut icon" href={`${serverIcon}?v=3`} type="image/png" />
      <link rel="apple-touch-icon" href={`${serverIcon}?v=3`} />
    </>
  );
}
