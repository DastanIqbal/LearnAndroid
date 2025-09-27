// Sample JavaScript Library for JSEngineManager Demo
// This file demonstrates loading external JavaScript libraries

// Utility functions
function greetUser(name) {
    return `Hello, ${name}! Welcome to the JavaScript Engine.`;
}

function calculateFactorial(n) {
    if (n <= 1) return 1;
    return n * calculateFactorial(n - 1);
}

function processArray(arr, operation) {
    switch(operation) {
        case 'sum':
            return arr.reduce((a, b) => a + b, 0);
        case 'average':
            return arr.reduce((a, b) => a + b, 0) / arr.length;
        case 'max':
            return Math.max(...arr);
        case 'min':
            return Math.min(...arr);
        default:
            return arr;
    }
}

// Object-oriented example
class Calculator {
    constructor() {
        this.history = [];
    }
    
    add(a, b) {
        const result = a + b;
        this.history.push(`${a} + ${b} = ${result}`);
        return result;
    }
    
    subtract(a, b) {
        const result = a - b;
        this.history.push(`${a} - ${b} = ${result}`);
        return result;    }
    
    multiply(a, b) {
        const result = a * b;
        this.history.push(`${a} * ${b} = ${result}`);
        return result;
    }
    
    divide(a, b) {
        if (b === 0) throw new Error("Division by zero!");
        const result = a / b;
        this.history.push(`${a} / ${b} = ${result}`);
        return result;
    }
    
    getHistory() {
        return this.history;
    }
    
    clearHistory() {
        this.history = [];
    }
}

// Global calculator instance
const calc = new Calculator();

// Async simulation (using setTimeout isn't available in QuickJS, but we can simulate)
function delayedOperation(value, callback) {
    // In a real scenario, this might be an async operation
    const result = value * 2;
    if (typeof callback === 'function') {
        callback(result);
    }
    return result;
}

// Testing Kotlin bindings
function testKotlinBindings() {
    try {
        // Test bound Kotlin function
        const multiplyResult = multiply(5, 7);
        console.log("Kotlin multiply function result:", multiplyResult);
        
        // Test bound Kotlin object  
        if (typeof KotlinData !== 'undefined') {
            console.log("Kotlin data:", JSON.stringify(KotlinData, null, 2));
        }
        
        // Test void function
        showToast("Hello from JavaScript!");
        
        return "Kotlin bindings tested successfully!";
    } catch (error) {
        return "Error testing Kotlin bindings: " + error.message;
    }
}

// Export message
console.log("Sample library loaded successfully!");
"Library loaded with functions: greetUser, calculateFactorial, processArray, Calculator class, and testKotlinBindings";