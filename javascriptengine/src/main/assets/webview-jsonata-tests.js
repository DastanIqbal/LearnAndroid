// WebView JSONata Test Suite
// This file contains comprehensive JSONata tests specifically for WebView execution

// Sample data for JSONata testing
const sampleData = {
    "company": "TechCorp",
    "employees": [
        {
            "id": 1,
            "name": "Alice Johnson",
            "age": 30,
            "department": "Engineering",
            "role": "Senior Developer",
            "salary": 95000,
            "skills": ["JavaScript", "React", "Node.js", "Python"],
            "projects": ["WebApp", "MobileAPI"],
            "location": {
                "city": "San Francisco",
                "country": "USA"
            },
            "startDate": "2020-03-15"
        },
        {
            "id": 2,
            "name": "Bob Smith",
            "age": 25,
            "department": "Design",
            "role": "UI/UX Designer",
            "salary": 70000,
            "skills": ["Figma", "Photoshop", "Sketch", "CSS"],
            "projects": ["WebApp", "BrandingKit"],
            "location": {
                "city": "New York",
                "country": "USA"
            },
            "startDate": "2021-07-20"
        },
        {
            "id": 3,
            "name": "Carol Williams",
            "age": 35,
            "department": "Engineering",
            "role": "Tech Lead",
            "salary": 120000,
            "skills": ["Java", "Spring", "Docker", "Kubernetes"],
            "projects": ["BackendAPI", "Infrastructure"],
            "location": {
                "city": "Austin",
                "country": "USA"
            },
            "startDate": "2019-01-10"
        },
        {
            "id": 4,
            "name": "David Brown",
            "age": 28,
            "department": "Marketing",
            "role": "Digital Marketer",
            "salary": 65000,
            "skills": ["SEO", "SEM", "Analytics", "Content"],
            "projects": ["CampaignX", "BrandingKit"],
            "location": {
                "city": "Chicago",
                "country": "USA"
            },
            "startDate": "2022-02-01"
        }
    ],
    "projects": [
        {
            "name": "WebApp",
            "status": "active",
            "budget": 500000,
            "deadline": "2024-12-31",
            "technologies": ["React", "Node.js", "MongoDB"]
        },
        {
            "name": "MobileAPI",
            "status": "completed",
            "budget": 300000,
            "deadline": "2024-06-30",
            "technologies": ["Python", "FastAPI", "PostgreSQL"]
        },
        {
            "name": "BackendAPI",
            "status": "active",
            "budget": 750000,
            "deadline": "2025-03-15",
            "technologies": ["Java", "Spring", "MySQL"]
        },
        {
            "name": "Infrastructure",
            "status": "planning",
            "budget": 200000,
            "deadline": "2024-09-30",
            "technologies": ["Docker", "Kubernetes", "AWS"]
        },
        {
            "name": "CampaignX",
            "status": "active",
            "budget": 150000,
            "deadline": "2024-08-15",
            "technologies": ["Analytics", "CRM", "Email"]
        },
        {
            "name": "BrandingKit",
            "status": "completed",
            "budget": 100000,
            "deadline": "2024-04-30",
            "technologies": ["Design", "Figma", "Brand"]
        }
    ]
};

// JSONata test cases
const jsonataTests = [
    {
        name: "Basic Field Selection",
        description: "Select all employee names",
        expression: "employees.name",
        expectedCount: 4
    },
    {
        name: "Filtering by Age",
        description: "Find employees older than 30",
        expression: "employees[age > 30]",
        expectedCount: 1
    },
    {
        name: "Complex Object Construction",
        description: "Create summary objects with selected fields",
        expression: "employees.{\"name\": name, \"department\": department, \"salary\": salary}",
        expectedCount: 4
    },
    {
        name: "Aggregation - Count",
        description: "Count total number of employees",
        expression: "$count(employees)",
        expectedResult: 4
    },
    {
        name: "Aggregation - Sum",
        description: "Calculate total salary budget",
        expression: "$sum(employees.salary)",
        expectedResult: 350000
    },
    {
        name: "Aggregation - Average",
        description: "Calculate average salary",
        expression: "$average(employees.salary)",
        expectedResult: 87500
    },
    {
        name: "Array Flattening",
        description: "Get all skills across all employees",
        expression: "employees.skills[]",
        expectedMinCount: 10
    },
    {
        name: "Grouping by Department",
        description: "Group employees by department",
        expression: "employees{department: $}",
        expectedKeys: ["Engineering", "Design", "Marketing"]
    },
    {
        name: "Date Manipulation",
        description: "Find employees who started in 2020 or later",
        expression: "employees[startDate >= '2020-01-01']",
        expectedCount: 4
    },
    {
        name: "Nested Field Access",
        description: "Get all employee cities",
        expression: "employees.location.city",
        expectedCount: 4
    },
    {
        name: "Conditional Logic",
        description: "Classify employees by salary range",
        expression: "employees.{\"name\": name, \"salaryRange\": salary > 100000 ? \"High\" : salary > 70000 ? \"Medium\" : \"Low\"}",
        expectedCount: 4
    },
    {
        name: "String Operations",
        description: "Format employee names to uppercase",
        expression: "employees.$uppercase(name)",
        expectedCount: 4
    },
    {
        name: "Array Operations",
        description: "Find unique departments",
        expression: "$distinct(employees.department)",
        expectedCount: 3
    },
    {
        name: "Join Operations",
        description: "Match employees with their projects",
        expression: "employees.{\"name\": name, \"projectDetails\": $$.projects[name in $.projects]}",
        expectedCount: 4
    },
    {
        name: "Mathematical Operations",
        description: "Calculate annual salary cost with 20% benefits",
        expression: "$sum(employees.salary) * 1.2",
        expectedResult: 420000
    },
    {
        name: "Complex Filtering",
        description: "Find Engineering employees with React skills",
        expression: "employees[department='Engineering' and 'React' in skills]",
        expectedCount: 1
    },
    {
        name: "Sorting",
        description: "Sort employees by salary descending",
        expression: "employees^(>salary)",
        expectedCount: 4
    },
    {
        name: "Regex Matching",
        description: "Find employees with names containing 'o'",
        expression: "employees[/o/.test(name)]",
        expectedMinCount: 1
    }
];

// Function to run a single JSONata test
async function runJSONataTest(testCase, jsonataFunction) {
    try {
        const startTime = performance.now();
        
        // Create JSONata expression
        const expression = jsonataFunction(testCase.expression);
        
        // Evaluate expression
        const result = expression.evaluate(sampleData);
        
        const endTime = performance.now();
        const executionTime = endTime - startTime;
        
        // Validate result based on test expectations
        let validation = { passed: true, message: "Test passed" };
        
        if (testCase.expectedCount !== undefined) {
            const actualCount = Array.isArray(result) ? result.length : (result ? 1 : 0);
            if (actualCount !== testCase.expectedCount) {
                validation = {
                    passed: false,
                    message: `Expected count ${testCase.expectedCount}, got ${actualCount}`
                };
            }
        }
        
        if (testCase.expectedMinCount !== undefined) {
            const actualCount = Array.isArray(result) ? result.length : (result ? 1 : 0);
            if (actualCount < testCase.expectedMinCount) {
                validation = {
                    passed: false,
                    message: `Expected minimum count ${testCase.expectedMinCount}, got ${actualCount}`
                };
            }
        }
        
        if (testCase.expectedResult !== undefined) {
            if (result !== testCase.expectedResult) {
                validation = {
                    passed: false,
                    message: `Expected result ${testCase.expectedResult}, got ${result}`
                };
            }
        }
        
        if (testCase.expectedKeys !== undefined) {
            const actualKeys = Object.keys(result || {});
            const missingKeys = testCase.expectedKeys.filter(key => !actualKeys.includes(key));
            if (missingKeys.length > 0) {
                validation = {
                    passed: false,
                    message: `Missing expected keys: ${missingKeys.join(', ')}`
                };
            }
        }
        
        return {
            name: testCase.name,
            description: testCase.description,
            expression: testCase.expression,
            status: "success",
            result: result,
            validation: validation,
            executionTime: Math.round(executionTime * 100) / 100,
            timestamp: new Date().toISOString()
        };
        
    } catch (error) {
        return {
            name: testCase.name,
            description: testCase.description,
            expression: testCase.expression,
            status: "error",
            error: error.message,
            stack: error.stack,
            timestamp: new Date().toISOString()
        };
    }
}

// Main test runner for WebView JSONata tests
async function runWebViewJSONataTests(jsonataFunction) {
    if (!jsonataFunction || typeof jsonataFunction !== 'function') {
        return {
            testSuite: "WebView JSONata Tests",
            status: "error",
            error: "JSONata function not available or not a function",
            timestamp: new Date().toISOString()
        };
    }
    
    const startTime = performance.now();
    const results = [];
    
    for (const testCase of jsonataTests) {
        const result = await runJSONataTest(testCase, jsonataFunction);
        results.push(result);
    }
    
    const endTime = performance.now();
    const totalExecutionTime = Math.round((endTime - startTime) * 100) / 100;
    
    const successfulTests = results.filter(r => r.status === "success" && r.validation.passed);
    const failedTests = results.filter(r => r.status === "error" || !r.validation.passed);
    
    return {
        testSuite: "WebView JSONata Tests",
        status: "completed",
        summary: {
            totalTests: jsonataTests.length,
            successfulTests: successfulTests.length,
            failedTests: failedTests.length,
            passRate: Math.round((successfulTests.length / jsonataTests.length) * 100 * 100) / 100,
            totalExecutionTime: totalExecutionTime
        },
        results: results,
        sampleDataInfo: {
            employees: sampleData.employees.length,
            projects: sampleData.projects.length,
            company: sampleData.company
        },
        executedAt: new Date().toISOString(),
        userAgent: navigator.userAgent
    };
}

// Function to test JSONata library loading and run tests
async function testJSONataInWebView() {
    try {
        // Check if JSONata is available
        if (typeof jsonata === 'undefined' && typeof window.jsonata === 'undefined') {
            return {
                testSuite: "WebView JSONata Tests",
                status: "error",
                error: "JSONata library not loaded or not available in global scope",
                suggestion: "Ensure JSONata library is loaded before running tests",
                timestamp: new Date().toISOString()
            };
        }
        
        // Get JSONata function (try both global scope and window)
        const jsonataFunc = typeof jsonata !== 'undefined' ? jsonata : window.jsonata;
        
        // Test basic JSONata functionality first
        try {
            const testExpr = jsonataFunc('$');
            const testResult = testExpr.evaluate("Hello JSONata");
            
            if (testResult !== "Hello JSONata") {
                throw new Error("JSONata basic test failed");
            }
        } catch (error) {
            return {
                testSuite: "WebView JSONata Tests",
                status: "error",
                error: "JSONata library failed basic functionality test: " + error.message,
                timestamp: new Date().toISOString()
            };
        }
        
        // Run full test suite
        return await runWebViewJSONataTests(jsonataFunc);
        
    } catch (error) {
        return {
            testSuite: "WebView JSONata Tests",
            status: "error",
            error: "Unexpected error: " + error.message,
            stack: error.stack,
            timestamp: new Date().toISOString()
        };
    }
}

// Execute JSONata tests and return JSON result
testJSONataInWebView().then(result => JSON.stringify(result, null, 2));