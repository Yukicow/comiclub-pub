/**
 * Konva Editor의 offsetX offsetY는 node의 scale이 변경되면 offset값 자체는 변하지 않지만 offset을 이용한 동작을 할 때 scale값을 고려해 동작하는 듯 하다. 
 * 하지만 width자체가 늘어나면 offset은 처음 초기화된 그대로이기 때문에 동적으로 변경시켜 주어야 한다.
 */

const editor = document.querySelector("#main-editor");
const imgShower = document.querySelector(".img-shower");

const imgFileInput = document.querySelector("#img-file");
const textAddButton = document.querySelector(".text-add-button");

// Detail Option
const detailOptionBox = document.querySelector(".detail-option-box");
const drawingOptionBox = document.querySelector(".drawing-option-box");
const brushSelector = document.querySelector(".brush-selector");
const brushColorSelector = document.querySelector(".brush-color-selector");
const brushSizeSelector = document.querySelector(".brush-size-selector");
let brushColor = "black";
let isPaint = false;
let mode = "source-over";
let brushSize = 5;
let lastLine;

// Edit Mode
const editModeChanger = document.querySelector(".edit-mode-changer");
let editMode = true;

// Undo Redo
const undoButton = document.querySelector(".undo-button");
const redoButton = document.querySelector(".redo-button");
const undoStack = [];
const redoStack = [];

// Item Option
const circleShape = document.querySelector(".circle");
const rectShape = document.querySelector(".rect");
const ballon1 = document.querySelector(".ballon1");
const ballon2 = document.querySelector(".ballon2");

/**
 * Edit State (contextmenu에서 node의 상태를 관리하기 위해 필요한 변수들)
 * undo를 위해 saveLayer를 호출해야 하는데, input의 종류에 따라 데이터의 변화 이전 상태를 최초에만 저장할 수가 없음. 그래서 필요.
 */
// Image
let changingOpacity = false;
let changingMosaic = false;
let changingBlur = false;

// Text
let writingText = false;
let changingFontColor = false;
let changingFontStrokeColor = false;
let changingFontStrokeSize = false;
let textEditable = false;

// Shape
let changingShapeColor = false;
let changingShapeStrokeColor = false;
let changingShapeStrokeWidth = false;

// Undo Redo
let savingLayer = false;
let undoing = false;
let redoing = false;

// 레이어에 한 번이라도 변화가 있었는 지
let layerChanged = false;

let konvaStage = new Konva.Stage({
    container: "img-canvas",
    width: 900,
    height: 600,
});

konvaStage.addEventListener("contextmenu", (e) => {
    e.preventDefault();
});

let konvaLayer = new Konva.Layer();
konvaStage.add(konvaLayer);
initStage();

function initStage() {
    konvaStage.on("mousedown", function (e) {
        if (editMode) return;
        saveCurrentLayerState(); // 그림을 그리기 전의 상태를 저장

        isPaint = true;
        const pos = konvaStage.getPointerPosition();
        lastLine = new Konva.Line({
            stroke: brushColor,
            strokeWidth: brushSize,
            globalCompositeOperation: mode,
            // round cap for smoother lines
            lineCap: "round",
            lineJoin: "round",
            // add point twice, so we have some drawings even on a simple click
            points: [pos.x, pos.y, pos.x, pos.y],
        });
        konvaLayer.add(lastLine);
    });

    // and core function - drawing
    konvaStage.on("mousemove", function (e) {
        if (!isPaint) {
            return;
        }

        // prevent scrolling on touch devices
        e.evt.preventDefault();

        const pos = konvaStage.getPointerPosition();
        const newPoints = lastLine.points().concat([pos.x, pos.y]);
        lastLine.points(newPoints);
    });
}

document.addEventListener("mousedown", () => {
    initEditState();
});

document.addEventListener("mouseup", () => {
    isPaint = false;
});

document.addEventListener("keydown", (e) => {
    if (e.ctrlKey && e.key === "z") {
        initEditState();
        undoStage();
    }
});

document.addEventListener("keydown", (e) => {
    if (e.ctrlKey && e.key === "y") {
        redoStage();
    }
});

function initEditState() {
    writingText = false;
    textEditable = false;
    changingFontColor = false;
    changingFontStrokeColor = false;
    changingOpacity = false;
    changingMosaic = false;
    changingBlur = false;
    document.querySelectorAll(".text-input").forEach((textarea) => {
        textarea.remove();
    });
    konvaLayer.find("Text").forEach((text) => {
        text.setAttr("visible", true);
    });
    konvaLayer.find("Transformer").forEach((transformer) => {
        transformer.visible(false);
    });
    document.querySelectorAll(".ind-option-box").forEach((box) => box.remove());
    document.querySelectorAll(".node-context-menu").forEach((option) => {
        option.remove();
    });
}

textAddButton.addEventListener("click", () => {
    saveCurrentLayerState(); // 새로운 Node가 생성되기 전의 상태를 저장
    createTextNode();
});

detailOptionBox.addEventListener("mousedown", (e) => {
    e.stopPropagation();
    document
        .querySelectorAll(".node-context-menu")
        .forEach((box) => box.remove());
});

imgFileInput.addEventListener("input", () => {
    saveCurrentLayerState(); // 새로운 Node가 생성되기 전의 상태를 저장

    const file = imgFileInput.files[0];
    imgFileInput.value = null;
    const url = URL.createObjectURL(file);
    // 이미지 로드'
    createKonvaImage(url);
});

function createKonvaImage(url) {
    var imageObj = new Image();
    imageObj.onload = function () {
        const group = new Konva.Group();

        // Konva.Image 생성
        var imageNode = new Konva.Image({
            image: imageObj,
            x: konvaStage.width() / 2,
            y: konvaStage.height() / 2,
            opacity: 1,
            blurRadius: 0,
            pixelSize: 1,
            draggable: editMode ? true : false,
        });

        setSizeOfImage(imageObj, imageNode);

        // 좌우 반전 기능을 위함
        imageNode.offsetX(imageNode.width() / 2);
        imageNode.offsetY(imageNode.height() / 2);

        imageNode.cache();
        imageNode.filters([Konva.Filters.Blur, Konva.Filters.Pixelate]);

        // Transformer 생성
        const transformer = new Konva.Transformer({
            nodes: [imageNode],
            visible: false,
            anchorDragBoundFunc: function (oldPos, newPos, event) {
                const cellWidth = konvaStage.width();
                const cellHeight = konvaStage.height();

                if (transformer.getActiveAnchor() === "rotater") {
                    return newPos;
                }

                const dist = Math.sqrt(
                    Math.pow(newPos.x - oldPos.x, 2) +
                        Math.pow(newPos.y - oldPos.y, 2)
                );

                // do not do any snapping with new absolute position (pointer position)
                // is too far away from old position
                if (dist > 20) {
                    return newPos;
                }

                const closestX = Math.round(newPos.x / cellWidth) * cellWidth;
                const diffX = Math.abs(newPos.x - closestX);

                const closestY = Math.round(newPos.y / cellHeight) * cellHeight;
                const diffY = Math.abs(newPos.y - closestY);

                const snappedX = diffX < 10;
                const snappedY = diffY < 10;

                // a bit different snap strategies based on snap direction
                // we need to reuse old position for better UX
                if (snappedX && !snappedY) {
                    return {
                        x: closestX,
                        y: oldPos.y,
                    };
                } else if (snappedY && !snappedX) {
                    return {
                        x: oldPos.x,
                        y: closestY,
                    };
                } else if (snappedX && snappedY) {
                    return {
                        x: closestX,
                        y: closestY,
                    };
                }
                return newPos;
            },
        });
        group.add(imageNode, transformer);
        konvaLayer.add(group);

        setDefaultNodeEvent(imageNode, transformer, group);
    };

    imageObj.crossOrigin = 'Anonymous';
    imageObj.src = url;
}

function setSizeOfImage(imageObj, imageNode) {
    const maxLength = 600; // 세로 가로 비율이 2 : 3이기 때문에 화면을 벗어나지 않으려면 작은 쪽인 세로의 최대 길이에 최대치를 맞춰야 함
    const maxWidthLength = konvaStage.width();
    const maxHeightLength = konvaStage.height();
    const smallerThanStage = imageObj.width <= maxWidthLength && imageObj.height <= maxHeightLength;
    const largerSide = imageObj.width >= imageObj.height ? "width" : "height";
    if(smallerThanStage){
        imageNode.width(imageObj.width);
        imageNode.height(imageObj.height);
    }else if (largerSide === "width") {
        imageNode.width(maxLength);
        imageNode.height(imageObj.height * (maxLength / imageObj.width));
    } else if (largerSide === "height"){
        imageNode.width(imageObj.width * (maxLength / imageObj.height));
        imageNode.height(maxLength);
    }
}

function createImageIndOptionBox(node) {
    // div 요소 생성
    var indOptionBox = document.createElement("div");
    indOptionBox.classList.add("ind-option-box");

    const opacityOption = document.createElement("div");
    opacityOption.classList.add("option");
    const opacityLabel = document.createElement("span");
    opacityLabel.innerText = "투명도";
    const opacityInput = document.createElement("input");
    opacityInput.setAttribute("type", "range");
    opacityInput.setAttribute("min", "0");
    opacityInput.setAttribute("max", "1");
    opacityInput.setAttribute("step", "0.05");
    opacityOption.appendChild(opacityLabel);
    opacityOption.appendChild(opacityInput);
    opacityInput.value = `${node.getOpacity()}`;
    opacityInput.addEventListener("input", () => {
        saveCurrentLayerState();
        if (!changingOpacity) {
            changingOpacity = true;
            saveCurrentLayerState();
        }
        node.opacity(opacityInput.value);
    });
    opacityInput.addEventListener("change", () => {
        changingOpacity = false;
    });

    const blurOption = document.createElement("div");
    blurOption.classList.add("option");
    const blurLabel = document.createElement("span");
    blurLabel.innerText = "흐림도";
    const blurInput = document.createElement("input");
    blurInput.setAttribute("type", "range");
    blurInput.setAttribute("min", "0");
    blurInput.setAttribute("max", "40");
    blurInput.setAttribute("step", "0.5");
    blurInput.value = `${node.getBlurRadius()}`;
    blurOption.appendChild(blurLabel);
    blurOption.appendChild(blurInput);
    blurInput.addEventListener("input", () => {
        if (!changingBlur) {
            changingBlur = true;
            saveCurrentLayerState();
        }
        node.blurRadius(blurInput.value);
    });
    blurInput.addEventListener("change", () => {
        changingBlur = false;
    });

    const mosaicOption = document.createElement("div");
    mosaicOption.classList.add("option");
    const mosaicLabel = document.createElement("span");
    mosaicLabel.innerText = "모자이크";
    const mosaicInput = document.createElement("input");
    mosaicInput.setAttribute("type", "range");
    mosaicInput.setAttribute("min", "1");
    mosaicInput.setAttribute("max", "20");
    mosaicInput.setAttribute("step", "1");
    mosaicOption.appendChild(mosaicLabel);
    mosaicOption.appendChild(mosaicInput);
    mosaicInput.value = `${node["pixelSize"]()}`;
    mosaicInput.addEventListener("input", () => {
        if (!changingMosaic) {
            changingMosaic = true;
            saveCurrentLayerState();
        }
        node["pixelSize"](parseFloat(mosaicInput.value));
    });
    mosaicInput.addEventListener("change", () => {
        changingMosaic = false;
    });

    const fitOption = document.createElement("div");
    fitOption.classList.add("option");
    const fitOptionLabel = document.createElement("div");
    fitOptionLabel.classList.add("fit-option-label");
    fitOptionLabel.innerText = "화면에 맞추기";
    fitOption.append(fitOptionLabel);
    fitOption.addEventListener("click", () => {
        saveCurrentLayerState();
        node.x(450);
        node.y(300);
        node.scaleX(900 / node.width());
        node.scaleY(600 / node.height());
    });

    // 부모 요소에 자식 요소 추가
    indOptionBox.appendChild(opacityOption);
    indOptionBox.appendChild(blurOption);
    indOptionBox.appendChild(mosaicOption);
    indOptionBox.appendChild(fitOption);
    detailOptionBox.appendChild(indOptionBox);

    detailOptionBox.prepend(indOptionBox);
}

function createTextNode() {
    const group = new Konva.Group();

    var textNode = new Konva.Text({
        x: konvaStage.width() / 2,
        y: konvaStage.height() / 2,
        fill: "black",
        fontSize: 30,
        fontStyle: "normal",
        textDecoration: "",
        strokeWidth: 0,
        stroke: undefined,
        text: "텍스트",
        opacity: 1,
        draggable: editMode ? true : false,
    });
    // 좌우 반전 기능을 위함
    textNode.offsetX(textNode.width() / 2);
    textNode.offsetY(textNode.height() / 2);

    var transformer = new Konva.Transformer({
        nodes: [textNode],
        visible: false,
        anchorDragBoundFunc: function (oldPos, newPos, event) {
            const cellWidth = konvaStage.width();
            const cellHeight = konvaStage.height();

            if (transformer.getActiveAnchor() === "rotater") {
                return newPos;
            }

            const dist = Math.sqrt(
                Math.pow(newPos.x - oldPos.x, 2) +
                    Math.pow(newPos.y - oldPos.y, 2)
            );

            // do not do any snapping with new absolute position (pointer position)
            // is too far away from old position
            if (dist > 20) {
                return newPos;
            }

            const closestX = Math.round(newPos.x / cellWidth) * cellWidth;
            const diffX = Math.abs(newPos.x - closestX);

            const closestY = Math.round(newPos.y / cellHeight) * cellHeight;
            const diffY = Math.abs(newPos.y - closestY);

            const snappedX = diffX < 10;
            const snappedY = diffY < 10;

            // a bit different snap strategies based on snap direction
            // we need to reuse old position for better UX
            if (snappedX && !snappedY) {
                return {
                    x: closestX,
                    y: oldPos.y,
                };
            } else if (snappedY && !snappedX) {
                return {
                    x: oldPos.x,
                    y: closestY,
                };
            } else if (snappedX && snappedY) {
                return {
                    x: closestX,
                    y: closestY,
                };
            }
            return newPos;
        },
    });
    group.add(textNode, transformer);
    konvaLayer.add(group);

    setDefaultNodeEvent(textNode, transformer, group);
    group.on("click", () =>
        setTimeout(() => {
            textEditable = true;
        }, 100)
    );
}

function setDefaultNodeEvent(node, transformer, group) {
    node.on("dragstart", () => {
        saveCurrentLayerState();
    });

    node.on("contextmenu", function (e) {
        e.evt.preventDefault();
        const optionList = document.querySelectorAll(".node-context-menu");
        optionList.forEach((option) => {
            initEditState();
            option.remove();
        });

        const menu = createContextmenu(group);
        document.body.append(menu);
        setContextMenuPosition(e, menu);
    });

    setCreateTextArea(node, transformer);

    transformer.on("transformstart", () => {
        saveCurrentLayerState();
    });

    /**
     * 테두리 사각형에 rotation값을 꼭 주어야 하는 이유
     * konva는 좌우 반전을 scaleX값을 음수로 바꾸는 것이 아닌, rotation의 방향을 기준으로
     * 우측, 좌측 상단을 기준으로 회전 시킨 뒤에 scaleY값을 변경시켜서 만드는 형태를 취하고 있다.
     * 따라서 rotation값을 이용해야 좌우 반전 상황에 대한 부분을 고려하여 코드를 작성할 수 있다.
     * Rect에도 좌우 반전을 적용시켜서 위치를 일치시켜야 하는데, 이 떄 rotation도 함께 사용해야만 한다는 것이다.
     * 그럼 rotation의 각도에 따라 반전 방향을 알 수 있고, 회전을 발생시켜 node와 일치하는 위치로 이동하게 된다.
     * 그래서 node와 같은 위치로 시작해서 같은 방향으로 회전 및 scale을 적용시키는 형태로 만들면 된다.
     * 하지만 이 방식을 취했더니 문제점이 있었다. 바로 strokeWidth가 scale이 적용되면서 함께 커져버린 것이다.
     * 그래서 이 문제를 해결하기 위해 착시 효과를 이용했다. node를 기준으로 해서 scale을 적용시키는 것이 아니라,
     * transformer를 기준으로 해서 Rect를 생성하는 것이다.
     * 이 방식의 문제점은 조금 복잡하다는 것인데, 대충 Rect가 좌우 반전의 영향을 받지 않는다는 점을 이용해서 어떻게든 node의 위치와 일치하게 만들었다고 생각하면 편하다.
     * 일단 transformer는 크기를 변경하고 나서 변화가 바로 적용되지 않는다. 좌측 상단이 계속 x,y 좌표이고 rotation도 그대로이다.
     * 하지만 일반 node들은 좌우 상하가 반전될 경우 기존의 좌측 상단이었던 x,y좌표가 반전되어 실제 좌표도 반전된 곳으로 이동되고, rotation값도 바로 반영된다.
     * transformer는 변화를 준 뒤에 drag를 통해 이동시키고 나면 다시 일반 node처럼 상태가 반영된다.
     * 대신 일반 node와 다른 점은 scale을 이용한 것이 아닌 스스로 변경된 width와 height값을 갖는다는 것이다.
     * 그래서 scale을 사용하지 않고도 원하는 크기를 지정할 수 있다는 점이 있어서 그걸 이용해서 scale없이
     * node의 위치에 같은 크기의 rect가 나타나는 것처럼 보이게 한 것이다.
     * 어차피 x,y위치는 똑같이 주어지기 때문에 같은 위치에 놓는 것은 똑같이 적용하기만 하면 된다.
     * 즉, width와 height의 적용만 다르게 한 것이다.
     * 이 점에 유의해서 코드를 대충 보고 해석해 보면 신기하게도 node의 위치에 가게끔 만들었다는 것을 알 수 있다.
     **/
    group.on("mouseenter", () => {
        if (transformer.visible() || !editMode) return; // 이미 편집 모드에 들어간 상태라면 return
        const border = new Konva.Rect({
            x: transformer.x(),
            y: transformer.y(),
            rotation: transformer.rotation(),
            width: transformer.width(),
            height: transformer.height(),
            stroke: "#64a0f7",
            strokeWidth: 1,
            name: "border",
        });
        group.add(border);
    });

    group.on("mouseleave", () => {
        if (!editMode) return;
        const border = group.findOne(".border");
        if (border) border.destroy();
    });

    group.on("mousedown", (e) => {
        e.evt.stopPropagation();
        initEditState();
        if (!editMode) return;
        const border = group.findOne(".border");
        if (border) border.destroy();
        transformer.visible(true);
        if (node instanceof Konva.Image) {
            createImageIndOptionBox(node);
        } else if (node instanceof Konva.Text) {
            createTextIndOptionBox(node);
        } else {
            createShapeIndOptionBox(node);
        }
    });
}

function createContextmenu(group) {
    // Create the main container element
    const container = document.createElement("div");
    container.classList.add("node-context-menu");
    container.addEventListener("contextmenu", (e) => {
        e.preventDefault();
    });
    container.addEventListener("mousedown", (e) => {
        e.stopPropagation();
    });

    // Create the "삭제하기" option
    const deleteOption = document.createElement("div");
    deleteOption.classList.add("option");
    deleteOption.innerText = "삭제하기";
    deleteOption.addEventListener("click", () => {
        const optionList = document.querySelectorAll(".node-context-menu");
        optionList.forEach((option) => {
            initEditState();
            option.remove();
        });
        saveCurrentLayerState();
        group.destroy();
    });

    const orderFirstOption = document.createElement("div");
    orderFirstOption.classList.add("option");
    orderFirstOption.innerText = "맨 앞으로";
    orderFirstOption.addEventListener("click", () => {
        const optionList = document.querySelectorAll(".node-context-menu");
        optionList.forEach((option) => {
            initEditState();
            option.remove();
        });
        saveCurrentLayerState();
        group.moveToTop();
    });

    const orderLastOption = document.createElement("div");
    orderLastOption.classList.add("option");
    orderLastOption.innerText = "맨 뒤로";
    orderLastOption.addEventListener("click", () => {
        const optionList = document.querySelectorAll(".node-context-menu");
        optionList.forEach((option) => {
            initEditState();
            option.remove();
        });
        saveCurrentLayerState();
        group.moveToBottom();
    });

    const copyOption = document.createElement("div");
    copyOption.classList.add("option");
    copyOption.innerText = "복사 붙여넣기";
    copyOption.addEventListener("click", () => {
        const optionList = document.querySelectorAll(".node-context-menu");
        optionList.forEach((option) => {
            initEditState();
            option.remove();
        });
        saveCurrentLayerState();
        group.getChildren().forEach((node) => {
            let copiedNode;
            switch (node.getClassName()) {
                case "Text":
                    copiedNode = copyTextNode(node, false);
                    break;
                case "Image":
                    copiedNode = copyImageNode(node, false);
                    break;
                case "Rect":
                    copiedNode = copyRectNode(node, false);
                    break;
                case "Circle":
                    copiedNode = copyCircleNode(node, false);
                    break;
                default:
                    break;
            }
            if (copiedNode) {
                konvaLayer.add(copiedNode);
            }
        });
    });

    //혹시 나중에 필요할 지 모르니 남겨 둠
    /*
    // Create the "투명도" option
    const opacityOption = document.createElement("div");
    opacityOption.classList.add("option");
    const opacityLabel = document.createElement("span");
    opacityLabel.innerText = "투명도";
    const opacityInput = document.createElement("input");
    opacityInput.setAttribute("type", "range");
    opacityInput.setAttribute("min", "0");
    opacityInput.setAttribute("max", "1");
    opacityInput.setAttribute("step", "0.05");
    opacityOption.appendChild(opacityLabel);
    opacityOption.appendChild(opacityInput);
    opacityInput.value = `${node.getOpacity()}`;

    opacityInput.addEventListener("input", () => {
        saveLayer();
        if (!changingOpacity) {
            changingOpacity = true;
            saveLayer();
        }
        node.opacity(opacityInput.value);
    });

    opacityInput.addEventListener("change", () => {
        changingOpacity = false;
    });


    const blurOption = document.createElement("div");
    blurOption.classList.add("option");
    const blurLabel = document.createElement("span");
    blurLabel.innerText = "흐림도";
    const blurInput = document.createElement("input");
    blurInput.setAttribute("type", "range");
    blurInput.setAttribute("min", "0");
    blurInput.setAttribute("max", "40");
    blurInput.setAttribute("step", "0.5");
    blurInput.value = `${node.getBlurRadius()}`;
    blurOption.appendChild(blurLabel);
    blurOption.appendChild(blurInput);
    blurInput.addEventListener("input", () => {
        if (!changingBlur) {
            changingBlur = true;
            saveLayer();
        }
        node.blurRadius(blurInput.value);
    })
    blurInput.addEventListener("change", () => {
        changingBlur = false;
    });



    const mosaicOption = document.createElement("div");
    mosaicOption.classList.add("option");
    const mosaicLabel = document.createElement("span");
    mosaicLabel.innerText = "모자이크";
    const mosaicInput = document.createElement("input");
    mosaicInput.setAttribute("type", "range");
    mosaicInput.setAttribute("min", "1");
    mosaicInput.setAttribute("max", "20");
    mosaicInput.setAttribute("step", "1");
    mosaicOption.appendChild(mosaicLabel);
    mosaicOption.appendChild(mosaicInput);
    mosaicInput.value = `${node['pixelSize']()}`;
    mosaicInput.addEventListener("input", () => {
        if (!changingMosaic) {
            changingMosaic = true;
            saveLayer();
        }
        node["pixelSize"](parseFloat(mosaicInput.value));
    });
    mosaicInput.addEventListener("change", () => {
        changingMosaic = false;
    }); */

    // Append all options to the container
    container.appendChild(deleteOption);
    container.appendChild(orderFirstOption);
    container.appendChild(orderLastOption);
    container.appendChild(copyOption);
    // container.appendChild(opacityOption);
    // container.appendChild(blurOption);
    // container.appendChild(mosaicOption);

    return container;
}

function createTextIndOptionBox(node) {
    var indOptionBox = document.createElement("div");
    indOptionBox.classList.add("ind-option-box");

    // 첫 번째 내부 div 요소 생성
    var option1 = document.createElement("div");
    option1.classList.add("option");

    // 두 번째 color-box 요소 생성
    var colorBox1 = document.createElement("div");
    colorBox1.classList.add("color-box");

    // span 요소 생성
    var fontColorSpan = document.createElement("span");
    fontColorSpan.textContent = "글자";

    // input 요소 생성
    var fontColorInput = document.createElement("input");
    fontColorInput.type = "color";
    fontColorInput.value = node.fill();
    fontColorInput.addEventListener("input", (e) => {
        if (!changingFontColor) {
            changingFontColor = true;
            saveCurrentLayerState();
        }
        node.fill(e.target.value);
    });
    fontColorInput.addEventListener("change", () => {
        changingFontColor = false;
    });

    colorBox1.append(fontColorSpan);
    colorBox1.append(fontColorInput);

    // div 요소 추가
    let boldExist = node.fontStyle().includes("bold");
    let italicExist = node.fontStyle().includes("italic");
    let underlineExist = node.textDecoration() === "underline";

    var fontWeightButton = document.createElement("div");
    boldExist
        ? fontWeightButton.classList.add(
              "weight-shower",
              "style-button",
              "style-on"
          )
        : fontWeightButton.classList.add("weight-shower", "style-button");
    fontWeightButton.textContent = "B";
    fontWeightButton.addEventListener("click", () => {
        saveCurrentLayerState();
        if (boldExist) {
            boldExist = false;
            fontWeightButton.classList.remove("style-on");
            node.fontStyle(getFontStyle());
        } else {
            boldExist = true;
            fontWeightButton.classList.add("style-on");
            node.fontStyle(getFontStyle());
        }
    });

    var fontItalicButton = document.createElement("div");
    italicExist
        ? fontItalicButton.classList.add(
              "italic-shower",
              "style-button",
              "style-on"
          )
        : fontItalicButton.classList.add("italic-shower", "style-button");
    fontItalicButton.textContent = "I";
    fontItalicButton.addEventListener("click", () => {
        saveCurrentLayerState();
        if (italicExist) {
            italicExist = false;
            fontItalicButton.classList.remove("style-on");
            node.fontStyle(getFontStyle());
        } else {
            italicExist = true;
            fontItalicButton.classList.add("style-on");
            node.fontStyle(getFontStyle());
        }
    });

    const getFontStyle = function () {
        let value = "";
        if (boldExist) value += "bold ";
        if (italicExist) value += "italic";
        if (value === "") value = "normal";
        return value;
    };

    var fontUnderLineButton = document.createElement("div");
    underlineExist
        ? fontUnderLineButton.classList.add(
              "underline-shower",
              "style-button",
              "style-on"
          )
        : fontUnderLineButton.classList.add("underline-shower", "style-button");
    fontUnderLineButton.textContent = "U";
    fontUnderLineButton.addEventListener("click", () => {
        saveCurrentLayerState();
        if (underlineExist) {
            underlineExist = false;
            fontUnderLineButton.classList.remove("style-on");
            node.textDecoration("");
        } else {
            underlineExist = true;
            fontUnderLineButton.classList.add("style-on");
            node.textDecoration("underline");
        }
    });

    // 첫 번째 내부 div 요소에 자식 요소 추가
    option1.appendChild(colorBox1);
    option1.appendChild(fontWeightButton);
    option1.appendChild(fontItalicButton);
    option1.appendChild(fontUnderLineButton);

    // 두 번째 내부 div 요소 생성
    var option2 = document.createElement("div");
    option2.classList.add("option");

    // select 요소 생성
    var fontFamilySelector = document.createElement("select");
    fontFamilySelector.classList.add("font-family-selector");

    // option 요소 추가
    var selectOption1 = document.createElement("option");
    selectOption1.textContent = "글꼴 선택";
    fontFamilySelector.appendChild(selectOption1);

    // 두 번째 내부 div 요소에 자식 요소 추가
    option2.appendChild(fontFamilySelector);

    // 세 번째 내부 div 요소 생성
    var option3 = document.createElement("div");
    option3.classList.add("option");

    // 두 번째 color-box 요소 생성
    var colorBox2 = document.createElement("div");
    colorBox2.classList.add("color-box");

    // span 요소 생성
    var colorBoxSpan2 = document.createElement("span");
    colorBoxSpan2.textContent = "글자 선";

    // input 요소 생성
    var fontStrokeColorInput = document.createElement("input");
    fontStrokeColorInput.type = "color";
    fontStrokeColorInput.value = node.stroke();
    fontStrokeColorInput.addEventListener("input", (e) => {
        if (!changingFontStrokeColor) {
            changingFontStrokeColor = true;
            saveCurrentLayerState();
        }
        node.stroke(fontStrokeColorInput.value);
    });
    fontStrokeColorInput.addEventListener("change", () => {
        changingFontStrokeColor = false;
    });

    var fontStrokeSizeInput = document.createElement("input");
    fontStrokeSizeInput.type = "number";
    fontStrokeSizeInput.min = "0";
    fontStrokeSizeInput.max = "10";
    fontStrokeSizeInput.step = "0.1";
    fontStrokeSizeInput.value = node.strokeWidth();
    fontStrokeSizeInput.addEventListener("input", () => {
        if (!changingFontStrokeSize) {
            changingFontStrokeSize = true;
            saveCurrentLayerState();
        }
        node.strokeWidth(parseFloat(fontStrokeSizeInput.value));
    });
    fontStrokeSizeInput.addEventListener("change", () => {
        changingFontStrokeSize = false;
    });

    // 두 번째 color-box 요소에 자식 요소 추가
    colorBox2.appendChild(colorBoxSpan2);
    colorBox2.appendChild(fontStrokeColorInput);
    option3.append(colorBox2);
    option3.append(fontStrokeSizeInput);

    // 부모 요소에 자식 요소 추가
    indOptionBox.appendChild(option2);
    indOptionBox.appendChild(option1);
    indOptionBox.appendChild(option3);
    detailOptionBox.appendChild(indOptionBox);

    detailOptionBox.prepend(indOptionBox);
}

function setCreateTextArea(textNode, transformer) {
    textNode.on("dblclick", (e) => {
        if (!textEditable) return;
        textNode.visible(false);

        textNode.offsetX(textNode.width() / 2);
        textNode.offsetY(textNode.height() / 2);

        // offsetX와 offsetY를 변경해서 node의 가운데로 이동시켰기 때문에, 회전의 축도 해당 위치가 기준점이 되었다.
        // 반전의 원리는 똑같이 scaleY값으로만 좌우 상하가 변경되는 것은 그대로이다. 따라서 scaleY값만 신경 써 주면 된다.
        // 하지만 기준점이 정가운데가 되어 버려서 scaleY값이 음수가 되어 반전이 일어나도 어차피 그 위치에사 상하만 바뀌게 된다.
        // 즉, x, y 값은 변화가 없다는 것이다. 하지만 정가운데인 기준점으로부터 x,y좌표값이 계산되기 때문에 그것만 신경써 주면 된다.
        const top =
            textNode.y() -
            Math.abs((textNode.height() * textNode.scaleY()) / 2);
        const left =
            textNode.x() - Math.abs((textNode.width() * textNode.scaleX()) / 2);

        // create textarea and style it
        const textarea = document.createElement("textarea");
        imgShower.appendChild(textarea);
        textarea.classList.add("text-input");
        textarea.value = textNode.text();
        textarea.style.fontFamily = textNode.fontFamily();
        textarea.style.color = textNode.fill();
        textarea.style.fontSize =
            Math.min(
                Math.abs(textNode.fontSize() * textNode.scaleX()),
                Math.abs(textNode.fontSize() * textNode.scaleY())
            ) + "px";
        textarea.style.lineHeight =
            Math.min(
                Math.abs(textNode.fontSize() * textNode.scaleX()),
                Math.abs(textNode.fontSize() * textNode.scaleY())
            ) + "px";
        textarea.style.position = "absolute";
        textarea.style.width =
            Math.abs(textNode.width() * textNode.scaleX()) + "px";
        textarea.style.height =
            Math.abs(textNode.height() * textNode.scaleY()) + "px";
        textarea.style.top = top + "px";
        textarea.style.left = left + "px";
        textarea.style.textDecoration =
            textNode.textDecoration() === "underline" ? "underline" : "none";
        textarea.style.fontWeight = textNode.fontStyle().includes("bold")
            ? "bold"
            : "normal";
        textarea.style.fontStyle = textNode.fontStyle().includes("italic")
            ? "italic"
            : "none";
        textarea.setAttribute("spellcheck", "false");
        textarea.focus();

        // textarea 회전
        const rotation = textNode.rotation();
        let transform = "";
        let px = 0;

        if (rotation) {
            transform += "rotateZ(" + rotation + "deg)";
        }

        const isFirefox =
            navigator.userAgent.toLowerCase().indexOf("firefox") > -1;
        if (isFirefox) {
            px += 2 + Math.round(textNode.fontSize() / 20);
        }
        transform += "translateY(-" + px + "px)";
        textarea.style.transform = transform;

        textarea.addEventListener("input", () => {
            if (!writingText) {
                writingText = true;
                // transformer.visible(false);
                saveCurrentLayerState();
            }

            const value = textarea.value;
            textNode.text(value);
            textNode.offsetX(textNode.width() / 2);
            textNode.offsetY(textNode.height() / 2);

            textarea.style.width =
                Math.abs(textNode.width() * textNode.scaleX()) + "px";
            textarea.style.height =
                Math.abs(textNode.height() * textNode.scaleY()) + "px";

            const top =
                textNode.y() -
                Math.abs((textNode.height() * textNode.scaleY()) / 2);
            const left =
                textNode.x() -
                Math.abs((textNode.width() * textNode.scaleX()) / 2);
            textarea.style.top = top + "px";
            textarea.style.left = left + "px";
        });

        // node의 scale이 변경되면 offset도 scale값을 이용해 함께 계산되지만
        // width자체가 늘어나면 offset은 처음 초기화된 그대로이기 때문에 동적으로 변경시켜 주어야 함
        textarea.addEventListener("change", () => {
            writingText = false;

            transformer.visible(true);
        });
        textarea.addEventListener("mousedown", (e) => {
            e.stopPropagation();
        });
        textarea.addEventListener("contextmenu", (e) => {
            e.preventDefault();
        });
    });
}

function setContextMenuPosition(e, menu) {
    const menuWidth = menu.offsetWidth;
    const menuHeight = menu.offsetHeight;
    let left = e.evt.clientX + window.pageXOffset;
    let top = e.evt.clientY + window.pageYOffset;
    left = left + menuWidth > window.innerWidth ? left - menuWidth : left;
    top = top + menuHeight > window.innerHeight ? top - menuHeight : top;
    menu.style.left = `${left}px`;
    menu.style.top = `${top}px`;
}

/**
 * Drawing Option
 **/
editModeChanger.addEventListener("click", (e) => {
    if (editMode) {
        drawingOptionBox.classList.remove("none");
        editModeChanger.innerText = "편집";
        changeNodeEditState(false);
        editMode = false;
    } else {
        drawingOptionBox.classList.add("none");
        editModeChanger.innerText = "그리기";
        changeNodeEditState(true);
        editMode = true;
    }
});

brushSelector.addEventListener("change", (e) => {
    mode = e.target.value === "brush" ? "source-over" : "destination-out";
});
brushColorSelector.addEventListener("input", (e) => {
    brushColor = e.target.value;
});
brushSizeSelector.addEventListener("input", (e) => {
    const value = e.target.value;
    if (value > 999) {
        e.target.value = 999;
        brushSize = 999;
    } else {
        brushSize = value;
    }
});

/**
 * Item Option
 */

circleShape.addEventListener("click", () => {
    saveCurrentLayerState(); // 새로운 Node가 생성되기 전의 상태를 저장

    const group = new Konva.Group();

    const circleNode = new Konva.Circle({
        x: konvaStage.width() / 2,
        y: konvaStage.height() / 2,
        radius: 150,
        stroke: "black",
        strokeWidth: 2,
        blurRadius: 0,
        pixelSize: 1,
        cached: true,
        draggable: editMode ? true : false,
    });
    circleNode.cache();
    circleNode.filters([Konva.Filters.Blur, Konva.Filters.Pixelate]);

    const transformer = new Konva.Transformer({
        nodes: [circleNode],
        visible: false,
        anchorDragBoundFunc: function (oldPos, newPos, event) {
            const cellWidth = konvaStage.width();
            const cellHeight = konvaStage.height();

            if (transformer.getActiveAnchor() === "rotater") {
                return newPos;
            }

            const dist = Math.sqrt(
                Math.pow(newPos.x - oldPos.x, 2) +
                    Math.pow(newPos.y - oldPos.y, 2)
            );

            // do not do any snapping with new absolute position (pointer position)
            // is too far away from old position
            if (dist > 20) {
                return newPos;
            }

            const closestX = Math.round(newPos.x / cellWidth) * cellWidth;
            const diffX = Math.abs(newPos.x - closestX);

            const closestY = Math.round(newPos.y / cellHeight) * cellHeight;
            const diffY = Math.abs(newPos.y - closestY);

            const snappedX = diffX < 10;
            const snappedY = diffY < 10;

            // a bit different snap strategies based on snap direction
            // we need to reuse old position for better UX
            if (snappedX && !snappedY) {
                return {
                    x: closestX,
                    y: oldPos.y,
                };
            } else if (snappedY && !snappedX) {
                return {
                    x: oldPos.x,
                    y: closestY,
                };
            } else if (snappedX && snappedY) {
                return {
                    x: closestX,
                    y: closestY,
                };
            }
            return newPos;
        },
    });

    setDefaultNodeEvent(circleNode, transformer, group);

    group.add(circleNode, transformer);
    konvaLayer.add(group);
});

rectShape.addEventListener("click", () => {
    saveCurrentLayerState(); // 새로운 Node가 생성되기 전의 상태를 저장

    const group = new Konva.Group();

    const rectNode = new Konva.Rect({
        x: konvaStage.width() / 2,
        y: konvaStage.height() / 2,
        width: 250,
        height: 250,
        stroke: "black",
        strokeWidth: 2,
        blurRadius: 0,
        pixelSize: 1,
        cached: true,
        draggable: editMode ? true : false,
    });

    rectNode.offsetX(rectNode.width() / 2);
    rectNode.offsetY(rectNode.height() / 2);

    // rectNode.cache();
    rectNode.filters([Konva.Filters.Blur, Konva.Filters.Pixelate]);

    const transformer = new Konva.Transformer({
        nodes: [rectNode],
        visible: false,
        anchorDragBoundFunc: function (oldPos, newPos, event) {
            const cellWidth = konvaStage.width();
            const cellHeight = konvaStage.height();

            if (transformer.getActiveAnchor() === "rotater") {
                return newPos;
            }

            const dist = Math.sqrt(
                Math.pow(newPos.x - oldPos.x, 2) +
                    Math.pow(newPos.y - oldPos.y, 2)
            );

            // do not do any snapping with new absolute position (pointer position)
            // is too far away from old position
            if (dist > 20) {
                return newPos;
            }

            const closestX = Math.round(newPos.x / cellWidth) * cellWidth;
            const diffX = Math.abs(newPos.x - closestX);

            const closestY = Math.round(newPos.y / cellHeight) * cellHeight;
            const diffY = Math.abs(newPos.y - closestY);

            const snappedX = diffX < 10;
            const snappedY = diffY < 10;

            // a bit different snap strategies based on snap direction
            // we need to reuse old position for better UX
            if (snappedX && !snappedY) {
                return {
                    x: closestX,
                    y: oldPos.y,
                };
            } else if (snappedY && !snappedX) {
                return {
                    x: oldPos.x,
                    y: closestY,
                };
            } else if (snappedX && snappedY) {
                return {
                    x: closestX,
                    y: closestY,
                };
            }
            return newPos;
        },
    });

    setDefaultNodeEvent(rectNode, transformer, group);

    transformer.on("transform", () => {
        if (rectNode.getAttr("cached")) rectNode.clearCache();
        rectNode.width(Math.abs(rectNode.width() * rectNode.scaleX()));
        rectNode.height(Math.abs(rectNode.height() * rectNode.scaleY()));
        rectNode.offsetX(rectNode.width() / 2);
        rectNode.offsetY(rectNode.height() / 2);
        rectNode.scaleX(1);
        rectNode.scaleY(1);
        rectNode.strokeWidth(rectNode.strokeWidth());
        rectNode.cache();
    });

    group.add(rectNode, transformer);
    konvaLayer.add(group);
});

ballon1.addEventListener("click", () =>{
    saveCurrentLayerState(); // 새로운 Node가 생성되기 전의 상태를 저장
    createKonvaImage("/img/canvas/ballon1.png");
});

ballon2.addEventListener("click", () =>{
    saveCurrentLayerState(); // 새로운 Node가 생성되기 전의 상태를 저장
    createKonvaImage("/img/canvas/ballon2.png");
});



function createShapeIndOptionBox(node) {
    // div 요소 생성
    const indOptionBox = document.createElement("div");
    indOptionBox.classList.add("ind-option-box");

    const option = document.createElement("div");
    option.classList.add("option");

    /**
     * 채우기
     */
    const colorBox1 = document.createElement("div");
    colorBox1.classList.add("color-box");

    const colorBoxSpan1 = document.createElement("span");
    colorBoxSpan1.textContent = "채우기";

    const fillColorInput = document.createElement("input");
    fillColorInput.type = "color";
    fillColorInput.value = node.fill();
    fillColorInput.addEventListener("input", (e) => {
        if (!changingShapeColor) {
            changingShapeColor = true;
            saveCurrentLayerState();
        }
        if (node.getAttr("cached")) node.clearCache();
        node.fill(fillColorInput.value);
        node.cache();
    });
    fillColorInput.addEventListener("change", () => {
        changingShapeColor = false;
    });

    colorBox1.append(colorBoxSpan1);
    colorBox1.append(fillColorInput);

    /**
     * 선
     */
    const colorBox2 = document.createElement("div");
    colorBox2.classList.add("color-box");

    const colorBoxSpan2 = document.createElement("span");
    colorBoxSpan2.textContent = "선";

    const strokeColorInput = document.createElement("input");
    strokeColorInput.type = "color";
    strokeColorInput.value = node.stroke();
    strokeColorInput.addEventListener("input", (e) => {
        if (!changingShapeStrokeColor) {
            changingShapeStrokeColor = true;
            saveCurrentLayerState();
        }
        if (node.getAttr("cached")) node.clearCache();
        node.stroke(strokeColorInput.value);
        node.cache();
    });
    strokeColorInput.addEventListener("change", () => {
        changingShapeStrokeColor = false;
    });

    const strokeWidthInput = document.createElement("input");
    strokeWidthInput.type = "number";
    strokeWidthInput.min = "0";
    strokeWidthInput.max = "100";
    strokeWidthInput.step = "0.5";
    strokeWidthInput.value = node.strokeWidth();
    strokeWidthInput.addEventListener("input", () => {
        if (!changingShapeStrokeWidth) {
            changingShapeStrokeWidth = true;
            saveCurrentLayerState();
        }
        if (node.getAttr("cached")) node.clearCache();
        node.strokeWidth(parseFloat(strokeWidthInput.value));
        node.cache();
    });
    strokeWidthInput.addEventListener("change", () => {
        changingFontStrokeSize = false;
    });

    colorBox2.appendChild(colorBoxSpan2);
    colorBox2.appendChild(strokeColorInput);

    option.append(colorBox1);
    option.append(colorBox2);
    option.append(strokeWidthInput);

    /**
     * 투명, 흐림, 모자이크
     */
    const opacityOption = document.createElement("div");
    opacityOption.classList.add("option");
    const opacityLabel = document.createElement("span");
    opacityLabel.innerText = "투명도";
    const opacityInput = document.createElement("input");
    opacityInput.setAttribute("type", "range");
    opacityInput.setAttribute("min", "0");
    opacityInput.setAttribute("max", "1");
    opacityInput.setAttribute("step", "0.05");
    opacityOption.appendChild(opacityLabel);
    opacityOption.appendChild(opacityInput);
    opacityInput.value = `${node.getOpacity()}`;
    opacityInput.addEventListener("input", () => {
        saveCurrentLayerState();
        if (!changingOpacity) {
            changingOpacity = true;
            saveCurrentLayerState();
        }
        node.opacity(opacityInput.value);
    });
    opacityInput.addEventListener("change", () => {
        changingOpacity = false;
    });

    const blurOption = document.createElement("div");
    blurOption.classList.add("option");
    const blurLabel = document.createElement("span");
    blurLabel.innerText = "흐림도";
    const blurInput = document.createElement("input");
    blurInput.setAttribute("type", "range");
    blurInput.setAttribute("min", "0");
    blurInput.setAttribute("max", "40");
    blurInput.setAttribute("step", "0.5");
    blurInput.value = `${node.getBlurRadius()}`;
    blurOption.appendChild(blurLabel);
    blurOption.appendChild(blurInput);
    blurInput.addEventListener("input", () => {
        if (!changingBlur) {
            changingBlur = true;
            saveCurrentLayerState();
        }
        node.blurRadius(blurInput.value);
    });
    blurInput.addEventListener("change", () => {
        changingBlur = false;
    });

    const mosaicOption = document.createElement("div");
    mosaicOption.classList.add("option");
    const mosaicLabel = document.createElement("span");
    mosaicLabel.innerText = "모자이크";
    const mosaicInput = document.createElement("input");
    mosaicInput.setAttribute("type", "range");
    mosaicInput.setAttribute("min", "1");
    mosaicInput.setAttribute("max", "20");
    mosaicInput.setAttribute("step", "1");
    mosaicOption.appendChild(mosaicLabel);
    mosaicOption.appendChild(mosaicInput);
    mosaicInput.value = `${node["pixelSize"]()}`;
    mosaicInput.addEventListener("input", () => {
        if (!changingMosaic) {
            changingMosaic = true;
            saveCurrentLayerState();
        }
        node["pixelSize"](parseFloat(mosaicInput.value));
    });
    mosaicInput.addEventListener("change", () => {
        changingMosaic = false;
    });

    const fitOption = document.createElement("div");
    fitOption.classList.add("option");
    const fitOptionLabel = document.createElement("div");
    fitOptionLabel.classList.add("fit-option-label");
    fitOptionLabel.innerText = "화면에 맞추기";
    fitOption.append(fitOptionLabel);
    fitOption.addEventListener("click", () => {
        saveCurrentLayerState();
        node.x(450);
        node.y(300);
        node.scaleX(900 / node.width());
        node.scaleY(600 / node.height());
    });

    indOptionBox.appendChild(option);
    indOptionBox.appendChild(opacityOption);
    indOptionBox.appendChild(blurOption);
    indOptionBox.appendChild(mosaicOption);
    detailOptionBox.appendChild(indOptionBox);

    detailOptionBox.prepend(indOptionBox);
}

/**
 * Undo Redo Option
 * */

undoButton.addEventListener("click", () => {
    undoStage();
});

redoButton.addEventListener("click", () => {
    redoStage();
});

function saveCurrentLayerState() {
    // 한 번도 layer가 변한 적이 없는 경우 불필요하게 서버로 요청하는 것을 막기 위해 초기화하는 변수
    if(!layerChanged) layerChanged = true;

    if (savingLayer) {
        setTimeout(saveCurrentLayerState, 100);
        return;
    }

    savingLayer = true;
    undoing = true;
    redoing = true;

    redoStack.length = 0;

    if (undoStack.length >= 20) {
        undoStack.shift();
    }

    const newLayer = createCopiedCurrentLayer();
    undoStack.push(newLayer);

    undoing = false;
    redoing = false;
    savingLayer = false;
}

function createCopiedCurrentLayer() {
    const newLayer = new Konva.Layer();
    konvaLayer.getChildren().forEach((child) => {
        if (child.getClassName() === "Group") {
            child.getChildren().forEach((node) => {
                switch (node.getClassName()) {
                    case "Text":
                        newLayer.add(copyTextNode(node));
                        break;
                    case "Image":
                        newLayer.add(copyImageNode(node));
                        break;
                    case "Rect":
                        newLayer.add(copyRectNode(node));
                        break;
                    case "Circle":
                        newLayer.add(copyCircleNode(node));
                        break;
                    default:
                        break;
                }
            });
        } else if (child.getClassName() === "Line") {
            newLayer.add(copyLineNode(child));
        }
    });
    return newLayer;
}

function undoStage() {
    if (undoStack.length < 1 || undoing) return;
    if (redoStack.length >= 20) redoStack.shift();
    undoing = true;

    const currentLayer = createCopiedCurrentLayer();
    redoStack.push(currentLayer);

    const newLayer = undoStack.pop();
    konvaLayer.destroy();
    konvaLayer = newLayer;
    konvaStage.add(newLayer);
    if (!editMode) changeNodeEditState(false);
    undoing = false;
}

function redoStage() {
    if (redoStack.length < 1 || redoing) return;
    if (undoStack.length >= 20) undoStack.shift();
    redoing = true;

    const currentLayer = createCopiedCurrentLayer();
    undoStack.push(currentLayer);

    const newLayer = redoStack.pop();
    konvaLayer.destroy();
    konvaLayer = newLayer;
    konvaStage.add(newLayer);
    if (!editMode) changeNodeEditState(false);
    redoing = false;
}

function copyTextNode(node, save = true) {
    const group = new Konva.Group();

    const textNode = new Konva.Text({
        x: save ? node.x() : konvaStage.width() / 2,
        y: save ? node.y() : konvaStage.height() / 2,
        rotation: node.rotation(),
        scaleX: node.scaleX(),
        scaleY: node.scaleY(),
        fill: node.fill(),
        stroke: node.stroke(),
        strokeWidth: node.strokeWidth(),
        fontSize: node.fontSize(),
        fontStyle: node.fontStyle(),
        text: node.text(),
        blurRadius: node.blurRadius(),
        opacity: node.opacity(),
        blurRadius: node.blurRadius(),
        pixelSize: node.pixelSize(),
        draggable: editMode ? true : false,
    });
    // 좌우 반전 기능을 위함
    textNode.offsetX(textNode.width() / 2);
    textNode.offsetY(textNode.height() / 2);

    const transformer = new Konva.Transformer({
        nodes: [textNode],
        visible: false,
        anchorDragBoundFunc: function (oldPos, newPos, event) {
            const cellWidth = konvaStage.width();
            const cellHeight = konvaStage.height();

            if (transformer.getActiveAnchor() === "rotater") {
                return newPos;
            }

            const dist = Math.sqrt(
                Math.pow(newPos.x - oldPos.x, 2) +
                    Math.pow(newPos.y - oldPos.y, 2)
            );

            // do not do any snapping with new absolute position (pointer position)
            // is too far away from old position
            if (dist > 20) {
                return newPos;
            }

            const closestX = Math.round(newPos.x / cellWidth) * cellWidth;
            const diffX = Math.abs(newPos.x - closestX);

            const closestY = Math.round(newPos.y / cellHeight) * cellHeight;
            const diffY = Math.abs(newPos.y - closestY);

            const snappedX = diffX < 10;
            const snappedY = diffY < 10;

            // a bit different snap strategies based on snap direction
            // we need to reuse old position for better UX
            if (snappedX && !snappedY) {
                return {
                    x: closestX,
                    y: oldPos.y,
                };
            } else if (snappedY && !snappedX) {
                return {
                    x: oldPos.x,
                    y: closestY,
                };
            } else if (snappedX && snappedY) {
                return {
                    x: closestX,
                    y: closestY,
                };
            }
            return newPos;
        },
    });
    group.add(textNode, transformer);

    setDefaultNodeEvent(textNode, transformer, group);
    group.on("click", () =>
        setTimeout(() => {
            textEditable = true;
        }, 100)
    );

    return group;
}

function copyImageNode(node, save = true) {
    const group = new Konva.Group();

    var imageNode = new Konva.Image({
        image: node.getAttr("image"),
        x: save ? node.x() : konvaStage.width() / 2,
        y: save ? node.y() : konvaStage.height() / 2,
        rotation: node.rotation(),
        scaleX: node.scaleX(),
        scaleY: node.scaleY(),
        width: node.width(),
        height: node.height(),
        opacity: node.opacity(),
        blurRadius: node.blurRadius(),
        pixelSize: node.pixelSize(),
        draggable: editMode ? true : false,
    });
    // 좌우 반전 기능을 위함
    imageNode.offsetX(imageNode.width() / 2);
    imageNode.offsetY(imageNode.height() / 2);

    imageNode.cache();
    imageNode.filters([Konva.Filters.Blur, Konva.Filters.Pixelate]);

    const transformer = new Konva.Transformer({
        nodes: [imageNode],
        visible: false,
        anchorDragBoundFunc: function (oldPos, newPos, event) {
            const cellWidth = konvaStage.width();
            const cellHeight = konvaStage.height();

            if (transformer.getActiveAnchor() === "rotater") {
                return newPos;
            }

            const dist = Math.sqrt(
                Math.pow(newPos.x - oldPos.x, 2) +
                    Math.pow(newPos.y - oldPos.y, 2)
            );

            // do not do any snapping with new absolute position (pointer position)
            // is too far away from old position
            if (dist > 20) {
                return newPos;
            }

            const closestX = Math.round(newPos.x / cellWidth) * cellWidth;
            const diffX = Math.abs(newPos.x - closestX);

            const closestY = Math.round(newPos.y / cellHeight) * cellHeight;
            const diffY = Math.abs(newPos.y - closestY);

            const snappedX = diffX < 10;
            const snappedY = diffY < 10;

            // a bit different snap strategies based on snap direction
            // we need to reuse old position for better UX
            if (snappedX && !snappedY) {
                return {
                    x: closestX,
                    y: oldPos.y,
                };
            } else if (snappedY && !snappedX) {
                return {
                    x: oldPos.x,
                    y: closestY,
                };
            } else if (snappedX && snappedY) {
                return {
                    x: closestX,
                    y: closestY,
                };
            }
            return newPos;
        },
    });
    group.add(imageNode, transformer);

    setDefaultNodeEvent(imageNode, transformer, group);

    return group;
}

function copyRectNode(node, save = true) {
    const group = new Konva.Group();

    let rectNode = new Konva.Rect({
        x: save ? node.x() : konvaStage.width() / 2,
        y: save ? node.y() : konvaStage.height() / 2,
        width: node.width(),
        height: node.height(),
        scaleX: node.scaleX(),
        scaleY: node.scaleY(),
        fill: node.fill(),
        stroke: node.stroke(),
        strokeWidth: node.strokeWidth(),
        opacity: node.opacity(),
        blurRadius: node.blurRadius(),
        pixelSize: node.pixelSize(),
        cached: true,
        draggable: editMode ? true : false,
    });
    // 좌우 반전 기능을 위함
    rectNode.offsetX(rectNode.width() / 2);
    rectNode.offsetY(rectNode.height() / 2);

    rectNode.cache();
    rectNode.filters([Konva.Filters.Blur, Konva.Filters.Pixelate]);

    const transformer = new Konva.Transformer({
        nodes: [rectNode],
        visible: false,
        anchorDragBoundFunc: function (oldPos, newPos, event) {
            const cellWidth = konvaStage.width();
            const cellHeight = konvaStage.height();

            if (transformer.getActiveAnchor() === "rotater") {
                return newPos;
            }

            const dist = Math.sqrt(
                Math.pow(newPos.x - oldPos.x, 2) +
                    Math.pow(newPos.y - oldPos.y, 2)
            );

            // do not do any snapping with new absolute position (pointer position)
            // is too far away from old position
            if (dist > 20) {
                return newPos;
            }

            const closestX = Math.round(newPos.x / cellWidth) * cellWidth;
            const diffX = Math.abs(newPos.x - closestX);

            const closestY = Math.round(newPos.y / cellHeight) * cellHeight;
            const diffY = Math.abs(newPos.y - closestY);

            const snappedX = diffX < 10;
            const snappedY = diffY < 10;

            // a bit different snap strategies based on snap direction
            // we need to reuse old position for better UX
            if (snappedX && !snappedY) {
                return {
                    x: closestX,
                    y: oldPos.y,
                };
            } else if (snappedY && !snappedX) {
                return {
                    x: oldPos.x,
                    y: closestY,
                };
            } else if (snappedX && snappedY) {
                return {
                    x: closestX,
                    y: closestY,
                };
            }
            return newPos;
        },
    });
    group.add(rectNode, transformer);

    transformer.on("transform", () => {
        if (rectNode.getAttr("cached")) rectNode.clearCache();
        rectNode.width(Math.abs(rectNode.width() * rectNode.scaleX()));
        rectNode.height(Math.abs(rectNode.height() * rectNode.scaleY()));
        rectNode.offsetX(rectNode.width() / 2);
        rectNode.offsetY(rectNode.height() / 2);
        rectNode.scaleX(1);
        rectNode.scaleY(1);
        rectNode.strokeWidth(rectNode.strokeWidth());
        rectNode.cache();
    });

    setDefaultNodeEvent(rectNode, transformer, group);

    return group;
}

function copyCircleNode(node, save = true) {
    const group = new Konva.Group();

    const circleNode = new Konva.Circle({
        x: save ? node.x() : konvaStage.width() / 2,
        y: save ? node.y() : konvaStage.height() / 2,
        width: node.width(),
        height: node.height(),
        scaleX: node.scaleX(),
        scaleY: node.scaleY(),
        radius: node.radius(),
        fill: node.fill(),
        stroke: node.stroke(),
        strokeWidth: node.strokeWidth(),
        opacity: node.opacity(),
        blurRadius: node.blurRadius(),
        pixelSize: node.pixelSize(),
        cached: true,
        draggable: editMode ? true : false,
    });

    circleNode.cache();
    circleNode.filters([Konva.Filters.Blur, Konva.Filters.Pixelate]);

    const transformer = new Konva.Transformer({
        nodes: [circleNode],
        visible: false,
        anchorDragBoundFunc: function (oldPos, newPos, event) {
            const cellWidth = konvaStage.width();
            const cellHeight = konvaStage.height();

            if (transformer.getActiveAnchor() === "rotater") {
                return newPos;
            }

            const dist = Math.sqrt(
                Math.pow(newPos.x - oldPos.x, 2) +
                    Math.pow(newPos.y - oldPos.y, 2)
            );

            // do not do any snapping with new absolute position (pointer position)
            // is too far away from old position
            if (dist > 20) {
                return newPos;
            }

            const closestX = Math.round(newPos.x / cellWidth) * cellWidth;
            const diffX = Math.abs(newPos.x - closestX);

            const closestY = Math.round(newPos.y / cellHeight) * cellHeight;
            const diffY = Math.abs(newPos.y - closestY);

            const snappedX = diffX < 10;
            const snappedY = diffY < 10;

            // a bit different snap strategies based on snap direction
            // we need to reuse old position for better UX
            if (snappedX && !snappedY) {
                return {
                    x: closestX,
                    y: oldPos.y,
                };
            } else if (snappedY && !snappedX) {
                return {
                    x: oldPos.x,
                    y: closestY,
                };
            } else if (snappedX && snappedY) {
                return {
                    x: closestX,
                    y: closestY,
                };
            }
            return newPos;
        },
    });
    group.add(circleNode, transformer);

    setDefaultNodeEvent(circleNode, transformer, group);

    return group;
}

function copyLineNode(node) {
    const line = new Konva.Line({
        stroke: node.getAttr("stroke"),
        strokeWidth: node.getAttr("strokeWidth"),
        globalCompositeOperation: node.getAttr("globalCompositeOperation"),
        // round cap for smoother lines
        lineCap: "round",
        lineJoin: "round",
        // add point twice, so we have some drawings even on a simple click
        points: node.getAttr("points"),
    });

    return line;
}

function changeNodeEditState(state) {
    const images = konvaLayer.find("Image");
    const texts = konvaLayer.find("Text");
    const rects = konvaLayer.find("Rect");
    const circles = konvaLayer.find("Circle");
    images.forEach((image) => {
        image.setAttr("draggable", state);
    });
    texts.forEach((text) => {
        text.setAttr("draggable", state);
    });
    rects.forEach((text) => {
        text.setAttr("draggable", state);
    });
    rects.forEach((rect) => {
        rect.setAttr("draggable", state);
    });
    circles.forEach((text) => {
        text.setAttr("draggable", state);
    });
    circles.forEach((circle) => {
        circle.setAttr("draggable", state);
    });
}

async function createImgFileBlob() {
    return new Promise((resolve) => {
        const imgUrl = konvaStage.toDataURL();
        const img = new Image();

        img.src = imgUrl;
        img.onload = async function () {
            const canvas = document.createElement("canvas");
            const context = canvas.getContext("2d");
            canvas.width = konvaStage.width();
            canvas.height = konvaStage.height();
            context.drawImage(img, 0, 0, img.width, img.height);

            canvas.toBlob(
                (blob) => {
                    resolve(blob);
                },
                "image/webp",
                100
            );
        };
    });
}
