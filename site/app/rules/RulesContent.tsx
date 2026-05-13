import type { ReactNode } from "react";
import { rulesMessages } from "./messages";
import styles from "./page.module.css";

function renderWithGray(text: string, keyBase: string): ReactNode {
  const nodes: React.ReactNode[] = [];
  let remaining = text;
  let i = 0;

  while (remaining.length > 0) {
    const match = remaining.match(/<gray>(.*?)<\/gray>/);
    if (!match || match.index === undefined) {
      nodes.push(remaining);
      break;
    }
    const idx = match.index;
    if (idx > 0) {
      nodes.push(remaining.slice(0, idx));
    }
    nodes.push(
      <span key={`${keyBase}-g-${i}`} className={styles.muted}>
        {match[1]}
      </span>,
    );
    remaining = remaining.slice(idx + match[0].length);
    i += 1;
  }

  return nodes.length === 1 ? nodes[0] : <>{nodes}</>;
}

function parseLine(raw: string, index: number): ReactNode {
  const trimmed = raw.trim();
  if (trimmed.length === 0) {
    return null;
  }

  const goldTitle = trimmed.match(/^<gold><b>(.*?)<\/b><\/gold>$/);
  if (goldTitle) {
    return (
      <h1 key={index} className={styles.docTitle}>
        {goldTitle[1]}
      </h1>
    );
  }

  const greenHeading = trimmed.match(
    /^<green>(?:<green>)?<i>(.*?)<\/i>$/,
  );
  if (greenHeading) {
    return (
      <h2 key={index} className={styles.sectionTitle}>
        {greenHeading[1]}
      </h2>
    );
  }

  const isIndented = raw.startsWith("  ");
  const body = trimmed
    .replace(/<gold>|<\/gold>|<b>|<\/b>|<green>|<\/green>|<i>|<\/i>/g, "");

  return (
    <p
      key={index}
      className={isIndented ? styles.ruleParagraphIndented : styles.ruleParagraph}
    >
      {renderWithGray(body, `l-${index}`)}
    </p>
  );
}

export default function RulesContent() {
  const elements: ReactNode[] = [];
  let blankRun = 0;

  rulesMessages.forEach((line, index) => {
    if (line.trim().length === 0) {
      blankRun += 1;
      return;
    }

    if (blankRun > 0) {
      elements.push(
        <div
          key={`spacer-${index}`}
          className={blankRun >= 2 ? styles.spacerLarge : styles.spacer}
          aria-hidden
        />,
      );
      blankRun = 0;
    }

    const node = parseLine(line, index);
    if (node) {
      elements.push(node);
    }
  });

  return (
    <section className={styles.section}>
      <div className={styles.panel}>{elements}</div>
    </section>
  );
}
