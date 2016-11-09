package components.application;


public class ReadyState extends WaitOpenDeliveryRequestState {
    public void enterState(MainController mainController) {
        mainController.modifyComputePlanningButtonDisabledProperty(false);
    }

    public void leaveState(MainController mainController) {

    }

    public MainControllerState onComputePlanningButtonAction(MainController mainController) {
        return new ComputingPlanningState(mainController);
    }
}
