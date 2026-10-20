import { useAuth } from "../../hooks/useAuth";
import { getUserDetails } from '../../utils/storage';
import { authService } from "../../services/authService";

const VerifyEmail = () => {

    const { sendEmailOTP } = useAuth();

    // Handle sending emmail for OTP email verification
    const handleSendEmailOTP = () => {
        try {
            // Send Email OTP
            const response = authService.sendEmailOTP(payload);
            if(response.data.success) {
                console.log(response.message || "Email OTP send successfully !!");
            }
        } catch(error) {
            if (error.response?.data) {
                console.log("Failed send email OTP: ", error.response.data.message || "Failed");
            } 
            else throw new Error("Response handling logic breaks !!")
        }
    }

    handleSendEmailOTP();

    return (
        <div>
            Verify Email
        </div>
    );
};

export default VerifyEmail;