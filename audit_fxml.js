const fs = require('fs');
const path = require('path');

function findFiles(dir, ext, results = []) {
  if (!fs.existsSync(dir)) return results;
  for (const f of fs.readdirSync(dir)) {
    const full = path.join(dir, f);
    const stat = fs.statSync(full);
    if (stat.isDirectory()) findFiles(full, ext, results);
    else if (f.endsWith(ext)) results.push(full);
  }
  return results;
}

const base = 'h:\\Heronix\\Heronix-SIS';
const resDir = path.join(base, 'src', 'main', 'resources');
const javaDir = path.join(base, 'src', 'main', 'java');

const fxmlFiles = findFiles(resDir, '.fxml');
const issues = [];

for (const fxmlPath of fxmlFiles.sort()) {
  const content = fs.readFileSync(fxmlPath, 'utf-8');
  const lines = content.split('\n');
  const relPath = path.relative(base, fxmlPath);

  // 1. Check controller
  const ctrlMatch = content.match(/fx:controller="([^"]+)"/);
  let ctrlContent = null;
  let ctrlClass = null;
  let ctrlPath = null;

  if (!ctrlMatch) {
    // Find line with root element
    issues.push(`NO CONTROLLER: ${relPath} - No fx:controller attribute found`);
  } else {
    ctrlClass = ctrlMatch[1];
    ctrlPath = path.join(javaDir, ...ctrlClass.split('.')) + '.java';
    if (!fs.existsSync(ctrlPath)) {
      const lineNum = lines.findIndex(l => l.includes('fx:controller')) + 1;
      issues.push(`MISSING CONTROLLER CLASS: ${relPath}:${lineNum} - ${ctrlClass} - file not found at ${path.relative(base, ctrlPath)}`);
    } else {
      ctrlContent = fs.readFileSync(ctrlPath, 'utf-8');
    }
  }

  // 2. Check fx:id fields
  const fxIdRegex = /fx:id="([^"]+)"/g;
  let m;
  while ((m = fxIdRegex.exec(content)) !== null) {
    const fxId = m[1];
    const lineNum = content.substring(0, m.index).split('\n').length;
    if (ctrlContent) {
      // Check if field exists in controller (as field name with word boundary)
      const fieldPattern = new RegExp('\\b' + fxId.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '\\s*[;=,)]');
      if (!fieldPattern.test(ctrlContent)) {
        issues.push(`MISSING @FXML FIELD: ${relPath}:${lineNum} - fx:id="${fxId}" not found in ${ctrlClass}`);
      }
    }
  }

  // 3. Check onAction handlers
  const actionRegex = /onAction="#([^"]+)"/g;
  while ((m = actionRegex.exec(content)) !== null) {
    const method = m[1];
    const lineNum = content.substring(0, m.index).split('\n').length;
    if (ctrlContent && !ctrlContent.includes(method)) {
      issues.push(`MISSING ACTION METHOD: ${relPath}:${lineNum} - onAction="#${method}" not in ${ctrlClass}`);
    }
  }

  // Check other event handlers
  for (const attr of ['onMouseClicked', 'onKeyPressed', 'onKeyReleased', 'onDragDetected', 'onDragDropped', 'onDragOver', 'onMouseEntered', 'onMouseExited']) {
    const hRegex = new RegExp(attr + '="#([^"]+)"', 'g');
    while ((m = hRegex.exec(content)) !== null) {
      const method = m[1];
      const lineNum = content.substring(0, m.index).split('\n').length;
      if (ctrlContent && !ctrlContent.includes(method)) {
        issues.push(`MISSING HANDLER: ${relPath}:${lineNum} - ${attr}="#${method}" not in ${ctrlClass}`);
      }
    }
  }

  // 4. Check stylesheet references
  const urlRegex = /<URL value="([^"]+)"/g;
  while ((m = urlRegex.exec(content)) !== null) {
    const url = m[1];
    const lineNum = content.substring(0, m.index).split('\n').length;
    if (url.startsWith('@')) {
      const cssPath = path.join(resDir, url.substring(1));
      if (!fs.existsSync(cssPath)) {
        issues.push(`MISSING CSS RESOURCE: ${relPath}:${lineNum} - "${url}" not found`);
      }
    }
  }

  // Check stylesheets= attribute
  const ssAttrRegex = /stylesheets="([^"]+)"/g;
  while ((m = ssAttrRegex.exec(content)) !== null) {
    const url = m[1];
    const lineNum = content.substring(0, m.index).split('\n').length;
    if (url.startsWith('@')) {
      const cssPath = path.join(resDir, url.substring(1));
      if (!fs.existsSync(cssPath)) {
        issues.push(`MISSING CSS RESOURCE: ${relPath}:${lineNum} - "${url}" not found`);
      }
    }
  }

  // Check image references
  const imgRegex = /<Image url="([^"]+)"/g;
  while ((m = imgRegex.exec(content)) !== null) {
    const url = m[1];
    const lineNum = content.substring(0, m.index).split('\n').length;
    if (url.startsWith('@')) {
      const imgPath = path.join(resDir, url.substring(1));
      if (!fs.existsSync(imgPath)) {
        issues.push(`MISSING IMAGE: ${relPath}:${lineNum} - "${url}" not found`);
      }
    }
  }
}

// Now check @FXML fields in controllers that have no matching fx:id
// Build ctrl->fxml map
const ctrlToFxml = {};
for (const fxmlPath of fxmlFiles) {
  const content = fs.readFileSync(fxmlPath, 'utf-8');
  const ctrlMatch = content.match(/fx:controller="([^"]+)"/);
  if (ctrlMatch) {
    ctrlToFxml[ctrlMatch[1]] = { path: fxmlPath, content };
  }
}

for (const [ctrlClass, fxmlInfo] of Object.entries(ctrlToFxml)) {
  const ctrlPath = path.join(javaDir, ...ctrlClass.split('.')) + '.java';
  if (!fs.existsSync(ctrlPath)) continue;
  const ctrlContent = fs.readFileSync(ctrlPath, 'utf-8');
  const ctrlLines = ctrlContent.split('\n');
  const relCtrlPath = path.relative(base, ctrlPath);

  // Find @FXML annotated fields
  for (let i = 0; i < ctrlLines.length; i++) {
    if (ctrlLines[i].includes('@FXML')) {
      // Look at next few lines for field declaration
      for (let j = i + 1; j < Math.min(i + 4, ctrlLines.length); j++) {
        const line = ctrlLines[j];
        if (line.includes('@') && !line.includes('@FXML')) continue;
        const fieldMatch = line.match(/\s*(private|protected|public)\s+([\w<>, ?]+)\s+(\w+)\s*[;=]/);
        if (fieldMatch) {
          const fieldType = fieldMatch[2];
          const fieldName = fieldMatch[3];
          // Skip non-UI types
          const uiTypes = ['Button','Label','TextField','TextArea','ComboBox','TableView','TableColumn','ListView','TreeView','CheckBox','RadioButton','ToggleButton','Slider','ProgressBar','ProgressIndicator','DatePicker','ColorPicker','ChoiceBox','Spinner','Tab','TabPane','SplitPane','ScrollPane','AnchorPane','BorderPane','GridPane','HBox','VBox','FlowPane','TilePane','StackPane','Pane','MenuBar','ToolBar','Pagination','Hyperlink','ImageView','WebView','HTMLEditor','TitledPane','Accordion','BarChart','LineChart','PieChart','AreaChart','Chart','Separator','Region','Node','MenuItem','Menu','TextFlow','Circle','Rectangle','Canvas'];
          const baseType = fieldType.split('<')[0].trim();
          if (uiTypes.includes(baseType)) {
            if (!fxmlInfo.content.includes(`fx:id="${fieldName}"`)) {
              issues.push(`ORPHAN @FXML FIELD: ${relCtrlPath}:${i + 1} - @FXML ${fieldType} ${fieldName} has no matching fx:id in ${path.relative(base, fxmlInfo.path)}`);
            }
          }
          break;
        }
        // Check if it's a method, not field
        if (line.match(/\s*(private|protected|public|void)\s+\w+\s*\(/)) break;
        if (line.trim() === '') continue;
      }
    }
  }
}

console.log(`\nTotal issues found: ${issues.length}\n`);
issues.sort().forEach(i => console.log(i));
