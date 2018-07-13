from events.device_wake_up_event import DeviceWakeUpEvent
from events.dispatch_flip_event import DispatchFlipEvent
from events.dispatch_key_event import DispatchKeyEvent
from events.dispatch_pointer_event import DispatchPointerEvent
from events.dispatch_press_event import DispatchPressEvent
from events.dispatch_string_event import DispatchStringEvent
from events.dispatch_trackball_event import DispatchTrackballEvent
from events.drag_event import DragEvent
from events.end_capture_app_framerate_event import EndCaptureAppFramerateEvent
from events.end_capture_framerate_event import EndCaptureFramerateEvent
from events.gui_gen_event import GUIGenEvent
from events.launch_activity_event import LaunchActivityEvent
from events.launch_instrumentation_event import LaunchInstrumentationEvent
from events.long_press_event import LongPressEvent
from events.pinch_zoom_event import PinchZoomEvent
from events.power_log_event import PowerLogEvent
from events.press_and_hold_event import PressAndHoldEvent
from events.profile_wait_event import ProfileWaitEvent
from events.rotate_screen_event import RotateScreenEvent
from events.run_cmd_event import RunCmdEvent
from events.start_capture_app_framerate_event import StartCaptureAppFramerateEvent
from events.start_capture_framerate_event import StartCaptureFramerateEvent
from events.tap_event import TapEvent
from events.user_wait_event import UserWaitEvent
from events.write_log_event import WriteLogEvent

event_classes = [
    DispatchPointerEvent,
    DispatchTrackballEvent,
    RotateScreenEvent,
    DispatchKeyEvent,
    DispatchFlipEvent,
    DispatchPressEvent,
    LaunchActivityEvent,
    LaunchInstrumentationEvent,
    UserWaitEvent,
    LongPressEvent,
    PowerLogEvent,
    WriteLogEvent,
    RunCmdEvent,
    TapEvent,
    ProfileWaitEvent,
    DeviceWakeUpEvent,
    DispatchStringEvent,
    PressAndHoldEvent,
    DragEvent,
    PinchZoomEvent,
    StartCaptureFramerateEvent,
    EndCaptureFramerateEvent,
    StartCaptureAppFramerateEvent,
    EndCaptureAppFramerateEvent,
    GUIGenEvent]