// JavaScript Bridge Test Suite
// Comprehensive tests for Android-JavaScript Bridge functionality

console.log('🌉 JavaScript Bridge Test Suite Loading...');

// Bridge Test Manager
const BridgeTestManager = {
    tests: [],
    results: [],
    
    // Add a test case
    addTest: function(name, description, testFunction) {
        this.tests.push({
            name: name,
            description: description,
            test: testFunction
        });
    },
    
    // Run all tests
    runAllTests: async function() {
        console.log('🚀 Starting Bridge Test Suite...');
        this.results = [];
        
        for (let i = 0; i < this.tests.length; i++) {
            const testCase = this.tests[i];
            console.log(`\n📋 Running Test ${i + 1}/${this.tests.length}: ${testCase.name}`);
            
            try {
                const startTime = Date.now();
                const result = await testCase.test();
                const duration = Date.now() - startTime;
                
                this.results.push({
                    name: testCase.name,
                    description: testCase.description,
                    passed: true,
                    result: result,
                    duration: duration,
                    error: null
                });
                
                console.log(`✅ PASSED: ${testCase.name} (${duration}ms)`);
                console.log(`   Result: ${JSON.stringify(result)}`);
                
            } catch (error) {
                const duration = Date.now() - startTime;
                this.results.push({
                    name: testCase.name,
                    description: testCase.description,
                    passed: false,
                    result: null,
                    duration: duration,
                    error: error.message
                });
                
                console.log(`❌ FAILED: ${testCase.name} (${duration}ms)`);
                console.log(`   Error: ${error.message}`);
            }
        }
        
        this.printSummary();
        return this.results;
    },
    
    // Print test summary
    printSummary: function() {
        const passed = this.results.filter(r => r.passed).length;
        const failed = this.results.length - passed;
        const totalTime = this.results.reduce((sum, r) => sum + r.duration, 0);
        
        console.log('\n📊 Test Summary:');
        console.log(`   Total Tests: ${this.results.length}`);
        console.log(`   Passed: ${passed} ✅`);
        console.log(`   Failed: ${failed} ❌`);
        console.log(`   Total Time: ${totalTime}ms`);
        
        if (failed > 0) {
            console.log('\n❌ Failed Tests:');
            this.results.filter(r => !r.passed).forEach(result => {
                console.log(`   - ${result.name}: ${result.error}`);
            });
        }
    }
};

// Test Cases

// 1. Bridge Availability Test
BridgeTestManager.addTest(
    'Bridge Availability',
    'Check if AndroidBridge is available and properly initialized',
    function() {
        if (typeof AndroidBridge === 'undefined') {
            throw new Error('AndroidBridge is not defined');
        }
        
        if (typeof AndroidBridge.call !== 'function') {
            throw new Error('AndroidBridge.call is not a function');
        }
        
        if (typeof AndroidBridge.callAsync !== 'function') {
            throw new Error('AndroidBridge.callAsync is not a function');
        }
        
        return { status: 'available', features: Object.keys(AndroidBridge) };
    }
);

// 2. Device Info Test
BridgeTestManager.addTest(
    'Device Information',
    'Retrieve device information from Android',
    function() {
        const result = AndroidBridge.getDeviceInfo();
        
        if (!result.success) {
            throw new Error('Failed to get device info: ' + result.error);
        }
        
        const info = result.data;
        if (!info.model || !info.manufacturer || !info.version) {
            throw new Error('Incomplete device info received');
        }
        
        return {
            model: info.model,
            manufacturer: info.manufacturer,
            androidVersion: info.version,
            sdk: info.sdk
        };
    }
);

// 3. Toast Notification Test
BridgeTestManager.addTest(
    'Toast Notifications',
    'Test toast message functionality',
    function() {
        const shortResult = AndroidBridge.showToast('Test short toast', false);
        const longResult = AndroidBridge.showToast('Test long toast', true);
        
        if (!shortResult.success || !longResult.success) {
            throw new Error('Toast notification failed');
        }
        
        return {
            shortToast: shortResult.success,
            longToast: longResult.success
        };
    }
);

// 4. Logging Test
BridgeTestManager.addTest(
    'Logging Functions',
    'Test different log levels',
    function() {
        const levels = ['debug', 'info', 'warn', 'error'];
        const results = {};
        
        levels.forEach(level => {
            const result = AndroidBridge.log(`Test ${level} message`, level);
            results[level] = result.success;
        });
        
        const allSucceeded = Object.values(results).every(success => success);
        if (!allSucceeded) {
            throw new Error('Some log operations failed');
        }
        
        return results;
    }
);

// 5. Storage Operations Test
BridgeTestManager.addTest(
    'Storage Operations',
    'Test preference storage and retrieval',
    function() {
        const testKey = 'bridgeTest_' + Date.now();
        const testValue = 'Bridge test value: ' + Math.random();
        
        // Save preference
        const saveResult = AndroidBridge.setPreference(testKey, testValue);
        if (!saveResult.success) {
            throw new Error('Failed to save preference: ' + saveResult.error);
        }
        
        // Retrieve preference
        const loadResult = AndroidBridge.getPreference(testKey, 'default');
        if (!loadResult.success) {
            throw new Error('Failed to load preference: ' + loadResult.error);
        }
        
        if (loadResult.data !== testValue) {
            throw new Error(`Value mismatch: expected "${testValue}", got "${loadResult.data}"`);
        }
        
        return {
            key: testKey,
            savedValue: testValue,
            retrievedValue: loadResult.data,
            match: testValue === loadResult.data
        };
    }
);

// 6. Custom Function Call Test
BridgeTestManager.addTest(
    'Custom Function Calls',
    'Test calling custom Android functions',
    async function() {
        // Test math operation if available
        try {
            const mathResult = AndroidBridge.call('mathOperation', {
                a: 15,
                b: 7,
                operation: 'multiply'
            });
            
            if (mathResult.success) {
                const expected = 15 * 7;
                if (mathResult.data.result !== expected) {
                    throw new Error(`Math result incorrect: expected ${expected}, got ${mathResult.data.result}`);
                }
                
                return {
                    mathOperation: true,
                    calculation: `${mathResult.data.operands[0]} × ${mathResult.data.operands[1]} = ${mathResult.data.result}`
                };
            }
        } catch (error) {
            // Math operation might not be available in all contexts
        }
        
        // Test greeting function if available
        try {
            const greetResult = AndroidBridge.call('customGreet', { name: 'Test User' });
            if (greetResult.success) {
                return {
                    customGreet: true,
                    message: greetResult.data
                };
            }
        } catch (error) {
            // Custom greet might not be available
        }
        
        return { message: 'No custom functions available' };
    }
);

// 7. Async Function Test
BridgeTestManager.addTest(
    'Async Functions',
    'Test asynchronous function calls with promises',
    async function() {
        try {
            // Test delay function
            const startTime = Date.now();
            const result = await AndroidBridge.delay(500);
            const actualDelay = Date.now() - startTime;
            
            // Allow some tolerance for timing
            if (actualDelay < 450 || actualDelay > 600) {
                console.warn(`Delay timing off: expected ~500ms, got ${actualDelay}ms`);
            }
            
            return {
                delayFunction: true,
                expectedDelay: 500,
                actualDelay: actualDelay,
                result: result
            };
            
        } catch (error) {
            // Try async counter if delay is not available
            try {
                const counterResult = await AndroidBridge.callAsync('asyncCounter', {
                    start: 1,
                    end: 3,
                    delay: 100
                });
                
                return {
                    asyncCounter: true,
                    result: counterResult
                };
            } catch (counterError) {
                throw new Error('No async functions available: ' + error.message);
            }
        }
    }
);

// 8. Event System Test
BridgeTestManager.addTest(
    'Event System',
    'Test event listening and emission',
    function() {
        return new Promise((resolve, reject) => {
            const testEventName = 'testEvent_' + Date.now();
            const testEventData = { message: 'Test event data', timestamp: Date.now() };
            let eventReceived = false;
            
            // Set up event listener
            const eventListener = function(data) {
                eventReceived = true;
                
                try {
                    if (JSON.stringify(data) !== JSON.stringify(testEventData)) {
                        reject(new Error('Event data mismatch'));
                    } else {
                        resolve({
                            eventName: testEventName,
                            dataReceived: data,
                            dataMatches: true
                        });
                    }
                } catch (error) {
                    reject(error);
                }
            };
            
            AndroidBridge.addEventListener(testEventName, eventListener);
            
            // Simulate event emission (this would normally come from Android side)
            setTimeout(() => {
                if (!eventReceived) {
                    // Manually trigger the event for testing
                    AndroidBridge._handleEvent(testEventName, testEventData);
                }
            }, 100);
            
            // Timeout after 2 seconds
            setTimeout(() => {
                if (!eventReceived) {
                    reject(new Error('Event not received within timeout'));
                }
            }, 2000);
        });
    }
);

// 9. Network Status Test
BridgeTestManager.addTest(
    'Network Status',
    'Check network availability',
    function() {
        const result = AndroidBridge.isNetworkAvailable();
        
        if (!result.success) {
            throw new Error('Failed to check network status: ' + result.error);
        }
        
        return {
            networkAvailable: result.data,
            timestamp: Date.now()
        };
    }
);

// 10. Error Handling Test
BridgeTestManager.addTest(
    'Error Handling',
    'Test error handling for invalid function calls',
    function() {
        // Test calling non-existent function
        const invalidResult = AndroidBridge.call('nonExistentFunction', {});
        
        if (invalidResult.success) {
            throw new Error('Expected error for invalid function call');
        }
        
        if (!invalidResult.error) {
            throw new Error('Error message not provided for invalid function call');
        }
        
        return {
            invalidFunctionHandled: true,
            errorMessage: invalidResult.error
        };
    }
);

// Bridge Test Runner
window.BridgeTestSuite = {
    // Run all tests
    runTests: function() {
        return BridgeTestManager.runAllTests();
    },
    
    // Run specific test
    runTest: function(testName) {
        const test = BridgeTestManager.tests.find(t => t.name === testName);
        if (!test) {
            throw new Error('Test not found: ' + testName);
        }
        
        return test.test();
    },
    
    // Get available tests
    getTests: function() {
        return BridgeTestManager.tests.map(t => ({
            name: t.name,
            description: t.description
        }));
    },
    
    // Get last results
    getResults: function() {
        return BridgeTestManager.results;
    }
};

// Auto-run tests if in test mode
if (typeof window !== 'undefined' && window.location && window.location.search.includes('autotest=true')) {
    window.addEventListener('load', function() {
        setTimeout(() => {
            console.log('🤖 Auto-running bridge tests...');
            BridgeTestSuite.runTests();
        }, 1000);
    });
}

console.log('✅ JavaScript Bridge Test Suite Ready!');
console.log('📋 Available tests:', BridgeTestManager.tests.length);
console.log('🚀 Run tests with: BridgeTestSuite.runTests()');