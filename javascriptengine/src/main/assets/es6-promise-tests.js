// ES6 and Promise Test Suite for AndroidX JavaScript Engine
// This file contains comprehensive tests for ES6 features and Promises

// Arrow Functions Test
function testArrowFunctions() {
    const add = (a, b) => a + b;
    const multiply = (x, y) => {
        const result = x * y;
        return result;
    };
    
    return {
        simpleArrow: add(3, 4),
        blockArrow: multiply(5, 6),
        message: "Arrow functions test completed"
    };
}

// Template Literals Test
function testTemplateLiterals() {
    const name = "JavaScript";
    const version = "ES6";
    const multiLine = `This is a
    multi-line
    string example`;
    
    return {
        basic: `Hello ${name}!`,
        expression: `Version: ${version} - ${2024}`,
        multiLine: multiLine.includes("multi-line"),
        message: "Template literals test completed"
    };
}

// Destructuring Test
function testDestructuring() {
    const array = [1, 2, 3, 4, 5];
    const [first, second, ...rest] = array;
    
    const obj = { x: 10, y: 20, z: 30 };
    const { x, y } = obj;
    
    return {
        arrayFirst: first,
        arraySecond: second,
        arrayRest: rest.length,
        objectX: x,
        objectY: y,
        sum: x + y,
        message: "Destructuring test completed"
    };
}

// Promise Basic Test
function testBasicPromise() {
    const promise = new Promise((resolve, reject) => {
        // Simulate async operation
        resolve("Promise resolved successfully!");
    });
    
    return {
        promiseType: typeof promise,
        isPromise: promise instanceof Promise,
        message: "Basic promise test completed"
    };
}

// Promise Methods Test
function testPromiseMethods() {
    const resolved = Promise.resolve("Resolved value");
    const rejected = Promise.reject("Rejected value");
    
    return {
        resolvedType: typeof resolved,
        rejectedType: typeof rejected,
        message: "Promise methods test completed"
    };
}

// Classes Test
function testClasses() {
    class Vehicle {
        constructor(type, speed) {
            this.type = type;
            this.speed = speed;
        }
        
        describe() {
            return `This ${this.type} travels at ${this.speed} km/h`;
        }
    }
    
    class Car extends Vehicle {
        constructor(brand, speed) {
            super("car", speed);
            this.brand = brand;
        }
        
        describe() {
            return `This ${this.brand} ${this.type} travels at ${this.speed} km/h`;
        }
    }
    
    const vehicle = new Vehicle("bike", 25);
    const car = new Car("Toyota", 120);
    
    return {
        vehicleDesc: vehicle.describe(),
        carDesc: car.describe(),
        message: "Classes test completed"
    };
}

// Spread Operator Test
function testSpreadOperator() {
    const arr1 = [1, 2, 3];
    const arr2 = [4, 5, 6];
    const combined = [...arr1, ...arr2];
    
    const obj1 = { a: 1, b: 2 };
    const obj2 = { c: 3, d: 4 };
    const mergedObj = { ...obj1, ...obj2 };
    
    return {
        combinedLength: combined.length,
        combinedValues: combined,
        mergedKeys: Object.keys(mergedObj).length,
        mergedObj: mergedObj,
        message: "Spread operator test completed"
    };
}

// Default Parameters Test
function testDefaultParameters() {
    function greet(name = "World", greeting = "Hello") {
        return `${greeting}, ${name}!`;
    }
    
    function calculate(a, b = 10, c = 5) {
        return a + b + c;
    }
    
    return {
        defaultGreeting: greet(),
        customGreeting: greet("Alice", "Hi"),
        defaultCalc: calculate(5),
        customCalc: calculate(5, 15, 10),
        message: "Default parameters test completed"
    };
}

// For...of Loop Test
function testForOfLoop() {
    const numbers = [1, 2, 3, 4, 5];
    let sum = 0;
    let product = 1;
    
    for (const num of numbers) {
        sum += num;
        product *= num;
    }
    
    const string = "hello";
    let chars = [];
    for (const char of string) {
        chars.push(char);
    }
    
    return {
        sum: sum,
        product: product,
        chars: chars,
        message: "For...of loop test completed"
    };
}

// Map and Set Test
function testMapAndSet() {
    const map = new Map();
    map.set('key1', 'value1');
    map.set('key2', 'value2');
    map.set('key3', 'value3');
    
    const set = new Set([1, 2, 3, 3, 4, 4, 5]);
    set.add(6);
    set.add(6); // Duplicate, should not be added
    
    return {
        mapSize: map.size,
        mapHasKey: map.has('key2'),
        mapValue: map.get('key1'),
        setSize: set.size,
        setHasValue: set.has(3),
        setValues: Array.from(set),
        message: "Map and Set test completed"
    };
}

// Comprehensive Test Runner
function runAllES6Tests() {
    const results = {
        arrowFunctions: testArrowFunctions(),
        templateLiterals: testTemplateLiterals(),
        destructuring: testDestructuring(),
        basicPromise: testBasicPromise(),
        promiseMethods: testPromiseMethods(),
        classes: testClasses(),
        spreadOperator: testSpreadOperator(),
        defaultParameters: testDefaultParameters(),
        forOfLoop: testForOfLoop(),
        mapAndSet: testMapAndSet(),
        timestamp: new Date().toISOString(),
        summary: "All ES6 tests completed successfully"
    };
    
    return JSON.stringify(results, null, 2);
}

// Export for testing (return the main function result)
runAllES6Tests();