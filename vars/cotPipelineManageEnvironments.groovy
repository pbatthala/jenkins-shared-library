#!/usr/bin/env groovy

@NonCPS
def createBooleanParameter(String desc, String value){

   return [$class: 'BooleanParameterDefinition', defaultValue: true, description: desc, name: value]
}

def call( String propertiesFile, String agentLabel ) {

    def defaultProperties = [DEPLOYMENT_MODE_LIST: 'update,stop', LEVELS_LIST: 'segment,solution,application', GENERATION_DEBUG: '', AUTOMATION_DEBUG: '' ]
    productProperties = readProperties interpolate: true, file: propertiesFile, defaults: defaultProperties;

    def basicParameters = input message: 'Please Provide Parameters', ok: 'Start', parameters: [
            choice(name: 'DEPLOYMENT_MODE', choices: "${productProperties["DEPLOYMENT_MODE_LIST"].split(",").join("\n")}", description: 'Desired way in which deploy should occur' ),
            choice(name: 'ENVIRONMENT', choices: "${productProperties["ENVIRONMENT_LIST"].split(",").join("\n")}", description: 'Environment to manage'),
            choice(name: 'SEGMENT', choices: "${productProperties["SEGMENT_LIST"].split(",").join("\n")}", description: 'Segment to manage' ),
            string(name: 'COMMENT', defaultValue: '', description: 'Added to the git commit message' ),
            booleanParam( name: 'TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT', defaultValue: false, description: 'Set this to force redeployment where only the runid value has changed. Mainly used where data is in S3.')
        ]

    // Levels
    def levelParameters = []
    productProperties["LEVELS_LIST"].split(",").each { 
        levelParameters += createBooleanParameter('Level', it )
    }
    def levelInputs = input(

        id: 'userInput', message: 'Component Levels', parameters: levelParameters
    )
    def levels = []
    levelInputs?.findAll{ it.value }?.each {
        levels += [ it.key.toString() ]
    }
    levels = levels.join(",")
    
    // Segment units
    def segmentUnitParameters = []
    productProperties["SEGMENT_UNITS"].split(",").each {
        segmentUnitParameters += createBooleanParameter('', it)
    }
    def segmentUnitInputs = input(

        id: 'userInput', message: 'Segment Level Units', parameters: segmentUnitParameters
    )
    def segmentUnits=[]
    segmentUnitInputs?.findAll{ it.value }?.each {
        segmentUnits += [ it.key.toString() ]
    }
    segmentUnits = segmentUnits.join(",")

    // Solution Units
    def solutionUnitParameters = []
    productProperties["SOLUTION_UNITS"].split(",").each {
        solutionUnitParameters += createBooleanParameter('', it)
    }
    def solutionUnitInputs = input(

        id: 'userInput', message: 'Solution Level Units', parameters: solutionUnitParameters
    )
    def solutionUnits=[]
    solutionUnitInputs?.findAll{ it.value }?.each {
        solutionUnits += [ it.key.toString() ]
    }
    solutionUnits = solutionUnits.join(",")

    // Application Units
    def applicationUnitParameters = []
    productProperties["APPLICATION_UNITS"].split(",").each {
        applicationUnitParameters += createBooleanParameter( '', it)
    }
    def appliationUnitInputs = input(

        id: 'userInput', message: 'Application Level Units', parameters: applicationUnitParameters
    )
    def applicationUnits=[]
    appliationUnitInputs?.findAll{ it.value }?.each {
        applicationUnits += [ it.key.toString() ]
    }
    applicationUnits = applicationUnits.join(",")
    
    // Call manage environment
    cotTaskManageEnvironment(
            propertiesFile: propertiesFile,
            deploymentMode: basicParameters["DEPLOYMENT_MODE"],
            environment: basicParameters["ENVIRONMENT"],
            segment: basicParameters["SEGMENT"],
            comment: basicParameters["COMMENT"],
            treatRunIdAsSignificant: basicParameters["TREAT_RUN_ID_DIFFERENCES_AS_SIGNIFICANT"],
            levelsList: levels,
            segmentUnits: segmentUnits,
            solutionUnits: solutionUnits,
            applicationUnits: applicationUnits)
}